package com.waypoint.resourcehub.domain.request;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssetUploadRequest {
  private UUID id;
  private Integer refTermId;
  private UUID refSetId;
  private String refTermName;
}