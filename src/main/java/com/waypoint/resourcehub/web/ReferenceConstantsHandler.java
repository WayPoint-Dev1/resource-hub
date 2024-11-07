package com.waypoint.resourcehub.web;

import static com.waypoint.resourcehub.constants.ResourceHubConstants.*;

import com.waypoint.resourcehub.domain.RefSetDTO;
import com.waypoint.resourcehub.service.ReferenceConstantsService;
import com.waypoint.resourcehub.service.S3Service;
import com.waypoint.resourcehub.utilities.ReferenceConstantsMapper;
import java.io.File;
import java.nio.file.FileSystems;
import java.util.UUID;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ReferenceConstantsHandler implements HandlerFunction<ServerResponse> {
  private final ReferenceConstantsService referenceConstantsService;
  private final S3Service s3Service;

  public ReferenceConstantsHandler(
      ReferenceConstantsService referenceConstantsService, S3Service s3Service) {
    this.referenceConstantsService = referenceConstantsService;
    this.s3Service = s3Service;
  }

  @Override
  public Mono<ServerResponse> handle(ServerRequest request) {
    return request
        .bodyToMono(RefSetDTO.class)
        .flatMap(ReferenceConstantsMapper::validateGetReferenceConstantsDetailsRequest)
        .flatMap(referenceConstantsService::getReferenceConstantsDetails)
        .flatMap(
            sample ->
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(sample));
  }

  public Mono<ServerResponse> handleAssetUpload(ServerRequest request) {
    return request
        .multipartData()
        .flatMap(ReferenceConstantsMapper::validateUploadAssetRequest)
        .flatMap(
            multiValueMap -> {
              FilePart filePart = (FilePart) multiValueMap.toSingleValueMap().get("file");
              FormFieldPart assetTypeIdPart =
                  (FormFieldPart) multiValueMap.toSingleValueMap().get("assetTypeId");
              File file =
                  new File(
                      System.getProperty("java.io.tmpdir")
                          + FileSystems.getDefault().getSeparator()
                          + filePart.filename());
              return DataBufferUtils.write(filePart.content(), file.toPath())
                  .then(
                      referenceConstantsService.uploadAsset(
                          UUID.fromString(assetTypeIdPart.value()), file));
            })
        .flatMap(
            assetDTO ->
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(assetDTO));
  }

  public Mono<ServerResponse> handleGetAsset(ServerRequest request) {
    return ReferenceConstantsMapper.validateGetAssetRequest(request)
        .flatMap(referenceConstantsService::getAsset)
        .flatMap(
            tuple2 -> {
              ByteArrayResource resource = new ByteArrayResource(tuple2.getT1());
              return ServerResponse.ok()
                  .contentType(
                      ReferenceConstantsMapper.determineMediaType(
                          tuple2.getT2().getExtensionType()))
                  .header(ASSET_EXTENSION_HEADER, tuple2.getT2().getExtensionType())
                  .header(ASSET_ID_HEADER, String.valueOf(tuple2.getT2().getId()))
                  .header(ASSET_TYPE_ID_HEADER, String.valueOf(tuple2.getT2().getAssetTypeId()))
                  .header(ASSET_PATH_HEADER, String.valueOf(tuple2.getT2().getAssetPath()))
                  .body(BodyInserters.fromResource(resource));
            });
  }
}
