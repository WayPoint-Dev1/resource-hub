package com.waypoint.resourcehub.domain;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefTermDTO {
  private UUID id;
  private Integer refTermId;
  private UUID refSetId;
  private String refTermName;
}