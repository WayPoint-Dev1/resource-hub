package com.waypoint.resourcehub.service;

import static com.waypoint.resourcehub.constants.ResourceHubConstants.CSV_FILE_EXTENSION;

import com.waypoint.resourcehub.exception.ErrorMessage;
import com.waypoint.resourcehub.exception.GenericException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Service
public class S3Service {

  private static final Logger log = LoggerFactory.getLogger(S3Service.class);
  private final S3Client s3Client;
  private final String bucketName;

  public S3Service(S3Client s3Client, @Value("${aws.s3.bucketName}") String bucketName) {
    this.s3Client = s3Client;
    this.bucketName = bucketName;
  }

  public Mono<byte[]> getFile(String key) {
    log.info("getFile :: {}", key);
    return Mono.fromCallable(
            () -> {
              GetObjectRequest getObjectRequest =
                  GetObjectRequest.builder().bucket(bucketName).key(key).build();
              ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
              try (ReadableByteChannel channel =
                  Channels.newChannel(
                      s3Client.getObjectAsBytes(getObjectRequest).asInputStream())) {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                while (channel.read(buffer) > 0) {
                  buffer.flip();
                  byteArrayOutputStream.write(buffer.array(), 0, buffer.limit());
                  buffer.clear();
                }
              } catch (IOException e) {
                log.info("getFile :: Exception :: {}", e);
                throw new RuntimeException(e);
              }
              return byteArrayOutputStream.toByteArray();
            })
        .onErrorResume(e -> Mono.error(new GenericException(ErrorMessage.S3_RETRIEVE_FAILED)));
  }

  public Flux<String> listCsvFiles(String prefix) {
    ListObjectsV2Request listReq =
        ListObjectsV2Request.builder().bucket(bucketName).prefix(prefix).build();

    ListObjectsV2Response listRes = s3Client.listObjectsV2(listReq);
    return Flux.fromIterable(listRes.contents())
        .map(S3Object::key)
        .filter(key -> key.endsWith(CSV_FILE_EXTENSION));
  }

  public Mono<Path> downloadFile(String key, Path destination) {
    return Mono.fromCallable(
            () -> {
              GetObjectRequest getReq =
                  GetObjectRequest.builder().bucket(bucketName).key(key).build();

              log.info(
                  "downloadFile :: Key :: {} :: Destination :: {}",
                  key,
                  destination.toAbsolutePath());
              // Ensure the destination directory exists
              Files.createDirectories(destination.getParent());

              s3Client.getObject(getReq, ResponseTransformer.toFile(destination));
              return destination;
            })
        .onErrorResume(e -> Mono.error(new GenericException(ErrorMessage.S3_DOWNLOAD_FAILED)));
  }

  public Mono<PutObjectResponse> uploadFile(File file, String key) {
    return Mono.fromCallable(
            () -> {
              log.info("uploadFile :: Key :: {} :: Bucket Name :: {}", key, bucketName);
              try {
                return s3Client.putObject(
                    PutObjectRequest.builder().bucket(bucketName).key(key).build(),
                    RequestBody.fromFile(file));
              } catch (S3Exception e) {
                log.info("uploadFile :: Exception :: {}", e);
                throw new RuntimeException(e);
              }
            })
        .onErrorResume(
            throwable -> Mono.error(new GenericException(ErrorMessage.S3_UPLOAD_FAILED)));
  }

  public Mono<DeleteObjectResponse> deleteFile(String key) {
    return Mono.fromCallable(
            () -> {
              log.info("deleteFile :: Key :: {} :: Bucket Name :: {}", key, bucketName);
              try {
                return s3Client.deleteObject(
                    DeleteObjectRequest.builder().bucket(bucketName).key(key).build());
              } catch (S3Exception e) {
                log.info("deleteFile :: Exception :: {}", e);
                throw new RuntimeException("Error deleting file from S3", e);
              }
            })
        .onErrorResume(
            throwable -> Mono.error(new GenericException(ErrorMessage.S3_CLEANUP_FAILED)));
  }
}
