package com.waypoint.resourcehub.domain.entity;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "REF_SET_TERM_VIEW")
@Getter
@Setter
@Builder
public class RefSetTermView {

  private UUID refSetUUID;
  private Integer refSetNumId;
  private String refSetName;
  @Id private UUID refTermUUID;
  private Integer refTermNumId;
  private String refTermName;
}
