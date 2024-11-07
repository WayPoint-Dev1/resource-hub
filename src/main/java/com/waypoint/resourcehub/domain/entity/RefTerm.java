package com.waypoint.resourcehub.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "ref_term")
@Getter
@Setter
@Builder
public class RefTerm {

  @Id private UUID id;
  private Integer refTermId;
  private UUID refSetId;
  private String refTermName;
  private Boolean isActive;
  private String createdBy;
  private LocalDateTime createdOn;
  private String updatedBy;
  private LocalDateTime updatedOn;
}
