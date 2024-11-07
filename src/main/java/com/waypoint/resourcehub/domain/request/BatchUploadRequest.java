package com.waypoint.resourcehub.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchUploadRequest {
  private String directoryPath;
  private boolean localBatchUpload;
}
