package com.waypoint.resourcehub.service;

import static com.waypoint.resourcehub.constants.ResourceHubConstants.*;
import static com.waypoint.resourcehub.exception.ErrorMessage.FAILED_TO_READ_BATCH_FILE;

import com.waypoint.resourcehub.constants.UploadStatus;
import com.waypoint.resourcehub.domain.entity.FileProcessingStatus;
import com.waypoint.resourcehub.domain.entity.RefSet;
import com.waypoint.resourcehub.domain.entity.RefTerm;
import com.waypoint.resourcehub.domain.request.BatchUploadRequest;
import com.waypoint.resourcehub.exception.ErrorMessage;
import com.waypoint.resourcehub.exception.GenericException;
import com.waypoint.resourcehub.repository.FileProcessingStatusRepository;
import com.waypoint.resourcehub.utilities.BatchUploadMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class BatchUploadService {
  private final FileProcessingStatusRepository fileProcessingStatusRepository;
  private final R2dbcEntityTemplate r2dbcEntityTemplate;
  private final S3Service s3Service;

  public BatchUploadService(
      FileProcessingStatusRepository fileProcessingStatusRepository,
      R2dbcEntityTemplate r2dbcEntityTemplate,
      S3Service s3Service) {
    this.fileProcessingStatusRepository = fileProcessingStatusRepository;
    this.r2dbcEntityTemplate = r2dbcEntityTemplate;
    this.s3Service = s3Service;
  }

  public Mono<List<FileProcessingStatus>> processLocalCsvFiles(
      BatchUploadRequest batchUploadRequest) {
    log.info("processLocalCsvFiles :: {}", batchUploadRequest);
    try {
      Stream<Path> stream = Files.list(Paths.get(batchUploadRequest.getDirectoryPath()));
      return Flux.fromStream(stream)
          .filter(Files::isRegularFile)
          .filter(
              path ->
                  path.getFileName()
                      .toString()
                      .matches(
                          REF_SET_BULK_UPLOAD_FILE_PREFIX
                              + ".*\\"
                              + CSV_FILE_EXTENSION
                              + "|"
                              + REF_TERM_BULK_UPLOAD_FILE_PREFIX
                              + ".*\\"
                              + CSV_FILE_EXTENSION))
          .flatMap(this::processFile)
          .collectList();
    } catch (IOException e) {
      return Mono.error(new GenericException(ErrorMessage.BATCH_FILES_NOT_READABLE));
    }
  }

  public Mono<List<FileProcessingStatus>> processS3CsvFiles(BatchUploadRequest batchUploadRequest) {
    log.info("processS3CsvFiles :: {}", batchUploadRequest);
    return s3Service
        .listCsvFiles(batchUploadRequest.getDirectoryPath())
        .flatMap(
            key -> {
              log.info("S3 File Path :: {}", key);
              return Mono.zip(
                  Mono.just(key),
                  fileProcessingStatusRepository.existsByFileNameAndStatus(
                      key.substring(batchUploadRequest.getDirectoryPath().length()),
                      UploadStatus.SUCCESS.name()));
            })
        .filter(tuple2 -> !tuple2.getT2())
        .flatMap(
            tuple2 -> {
              Path tempFile =
                  Paths.get(
                      System.getProperty("java.io.tmpdir"),
                      tuple2.getT1().substring(batchUploadRequest.getDirectoryPath().length()));
              return s3Service.downloadFile(tuple2.getT1(), tempFile);
            })
        .flatMap(
            path ->
                processFile(path)
                    .doFinally(
                        signalType -> {
                          try {
                            Files.deleteIfExists(path);
                            log.info("Deleted temp file :: {}", path.toAbsolutePath());
                          } catch (IOException e) {
                            log.error("Failed to delete temp file :: {}", path.toAbsolutePath());
                          }
                        }))
        .collectList();
  }

  private Mono<FileProcessingStatus> processFile(Path path) {
    log.info(" processFile :: Start :: {}", path);
    String fileName = path.getFileName().toString();
    Mono<FileProcessingStatus> statusRecord =
        fileProcessingStatusRepository
            .existsByFileNameAndStatus(fileName, UploadStatus.SUCCESS.name())
            .flatMap(
                aBoolean -> {
                  if (!aBoolean) {
                    return fileProcessingStatusRepository.save(
                        BatchUploadMapper.getFileProcessingStatus(
                            fileName, UploadStatus.PROCESSING, LocalDateTime.now()));
                  }
                  log.info("File {} has already been processed.", fileName);
                  return Mono.empty();
                });

    return statusRecord.flatMap(
        record -> {
          Flux<?> uploadData =
              fileName.startsWith(REF_SET_BULK_UPLOAD_FILE_PREFIX)
                  ? uploadRefSetData(path)
                  : uploadRefTermData(path);

          return uploadData
              .then(
                  fileProcessingStatusRepository.save(
                      record
                          .withStatus(UploadStatus.SUCCESS.name())
                          .withProcessedOn(LocalDateTime.now())))
              .onErrorResume(
                  e ->
                      fileProcessingStatusRepository.save(
                          record
                              .withStatus(UploadStatus.FAILURE.name())
                              .withProcessedOn(LocalDateTime.now())));
        });
  }

  public Flux<RefSet> uploadRefSetData(Path path) {
    log.info("uploadRefSetData :: {}", path.toAbsolutePath());
    return parseCsv(path, BatchUploadMapper::getRefSet)
        .flatMap(
            refSet -> {
              log.debug("refSet :: {}", refSet);
              return r2dbcEntityTemplate
                  .insert(RefSet.class)
                  .using(refSet)
                  .onErrorResume(
                      throwable -> {
                        log.info("uploadRefSetData :: Insert failed for refSet :: {}", refSet);
                        return Mono.empty();
                      });
            });
  }

  private Flux<RefTerm> uploadRefTermData(Path path) {
    log.info("uploadRefTermData :: {}", path.toAbsolutePath());
    return parseCsv(path, BatchUploadMapper::getRefTerm)
        .flatMap(
            refTerm -> {
              log.debug("refTerm :: {}", refTerm);
              return r2dbcEntityTemplate
                  .insert(RefTerm.class)
                  .using(refTerm)
                  .onErrorResume(
                      throwable -> {
                        log.info("uploadRefTermData :: Insert failed for refTerm :: {}", refTerm);
                        return Mono.empty();
                      });
            });
  }

  private <T> Flux<T> parseCsv(Path path, Function<CSVRecord, T> converter) {
    try {
      log.info("parseCsv :: {}", path.toAbsolutePath());
      CSVParser parser =
          CSVParser.parse(
              Files.newBufferedReader(path),
              CSVFormat.DEFAULT.withHeader().withSkipHeaderRecord(true));

      return Flux.fromIterable(parser.getRecords())
          .map(converter)
          .doOnError(e -> log.error("Error parsing CSV :: {}", e));

    } catch (IOException e) {
      log.info("parseCsv :: Exception :: {}", e);
      return Flux.error(new GenericException(FAILED_TO_READ_BATCH_FILE));
    }
  }
}
