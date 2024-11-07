package com.waypoint.resourcehub.domain;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.With;

import java.util.List;
import java.util.UUID;

@Builder
@Data
@With
@ToString
public class RefSetDTO {
  private UUID id;
  private Integer refSetId;
  private String refSetName;
  private List<RefTermDTO> refTermList;
}
