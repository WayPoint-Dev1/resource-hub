package com.waypoint.resourcehub.web;

import com.waypoint.resourcehub.domain.request.BatchUploadRequest;
import com.waypoint.resourcehub.service.BatchUploadService;
import com.waypoint.resourcehub.service.S3Service;
import com.waypoint.resourcehub.utilities.BatchUploadMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class BatchUploadHandler implements HandlerFunction<ServerResponse> {
  private final BatchUploadService batchUploadService;
  private final S3Service s3Service;

  public BatchUploadHandler(BatchUploadService batchUploadService, S3Service s3Service) {
    this.batchUploadService = batchUploadService;
    this.s3Service = s3Service;
  }

  @Override
  public Mono<ServerResponse> handle(ServerRequest request) {
    return request
        .bodyToMono(BatchUploadRequest.class)
        .flatMap(BatchUploadMapper::validateBatchUploadRequest)
        .flatMap(
            batchUploadRequest ->
                batchUploadRequest.isLocalBatchUpload()
                    ? batchUploadService.processLocalCsvFiles(batchUploadRequest)
                    : batchUploadService.processS3CsvFiles(batchUploadRequest))
        .flatMap(
            sample ->
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(sample));
  }
}
