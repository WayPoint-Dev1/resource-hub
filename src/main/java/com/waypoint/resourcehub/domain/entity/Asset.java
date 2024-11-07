package com.waypoint.resourcehub.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "asset")
@Getter
@Setter
@Builder
public class Asset {

  @Id private UUID id;
  private Integer assetId;
  private UUID assetTypeId;
  private String assetName;
  private String extensionType;
  private String assetPath;
  private Boolean isActive;
  private String createdBy;
  private LocalDateTime createdOn;
  private String updatedBy;
  private LocalDateTime updatedOn;

  @Override
  public String toString() {
    return "Asset{"
        + "id="
        + id
        + ", assetId="
        + assetId
        + ", assetTypeId="
        + assetTypeId
        + ", assetName='"
        + assetName
        + '\''
        + ", extensionType='"
        + extensionType
        + '\''
        + ", assetPath='"
        + assetPath
        + '\''
        + ", isActive="
        + isActive
        + ", createdBy='"
        + createdBy
        + '\''
        + ", createdOn="
        + createdOn
        + ", updatedBy='"
        + updatedBy
        + '\''
        + ", updatedOn="
        + updatedOn
        + '}';
  }
}
