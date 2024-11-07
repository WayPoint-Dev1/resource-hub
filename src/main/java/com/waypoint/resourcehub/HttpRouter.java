package com.waypoint.resourcehub;

import static com.waypoint.resourcehub.constants.ResourceHubConstants.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import com.waypoint.resourcehub.web.BatchUploadHandler;
import com.waypoint.resourcehub.web.ReferenceConstantsHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class HttpRouter {
  private final ReferenceConstantsHandler referenceConstantsHandler;
  private final BatchUploadHandler batchUploadHandler;

  public HttpRouter(
      ReferenceConstantsHandler referenceConstantsHandler, BatchUploadHandler batchUploadHandler) {
    this.referenceConstantsHandler = referenceConstantsHandler;
    this.batchUploadHandler = batchUploadHandler;
  }

  @Bean
  RouterFunction<ServerResponse> routes() {
    return route()
        .POST(
            FETCH_REFERENCE_CONSTANTS_URI,
            RequestPredicates.accept(MediaType.APPLICATION_JSON),
            referenceConstantsHandler)
        .POST(
            BATCH_UPLOAD_URI,
            RequestPredicates.accept(MediaType.APPLICATION_JSON),
            batchUploadHandler)
        .POST(
            ASSET_UPLOAD_URI,
            RequestPredicates.accept(MediaType.MULTIPART_FORM_DATA),
            referenceConstantsHandler::handleAssetUpload)
        .GET(
            GET_ASSET_URI,
            RequestPredicates.accept(MediaType.MULTIPART_FORM_DATA),
            referenceConstantsHandler::handleGetAsset)
        .build();
  }
}
