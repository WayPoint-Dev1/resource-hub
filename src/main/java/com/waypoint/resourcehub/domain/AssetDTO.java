package com.waypoint.resourcehub.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.With;
import org.springframework.data.annotation.Id;

@Builder
@Data
@With
public class AssetDTO {
  private UUID id;
  private Integer assetId;
  private UUID assetTypeId;
  private String assetName;
  private String extensionType;
  private String assetPath;
  private Boolean isActive;
}
