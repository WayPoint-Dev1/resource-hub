package com.waypoint.resourcehub.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "FILE_PROCESSING_STATUS")
@Getter
@Setter
@Builder
@With
public class FileProcessingStatus {
  @Id private UUID id;
  private String fileName;
  private String status; // Status could be "PENDING", "PROCESSING", "SUCCESS", "FAILURE"
  private LocalDateTime processedOn;
}
