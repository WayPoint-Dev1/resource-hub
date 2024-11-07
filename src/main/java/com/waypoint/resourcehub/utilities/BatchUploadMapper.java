package com.waypoint.resourcehub.utilities;

import com.waypoint.resourcehub.constants.UploadStatus;
import com.waypoint.resourcehub.domain.entity.FileProcessingStatus;
import com.waypoint.resourcehub.domain.entity.RefSet;
import com.waypoint.resourcehub.domain.entity.RefTerm;
import com.waypoint.resourcehub.domain.request.BatchUploadRequest;
import com.waypoint.resourcehub.exception.ErrorMessage;
import com.waypoint.resourcehub.exception.GenericException;
import java.time.LocalDateTime;
import java.util.UUID;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class BatchUploadMapper {

  private static final Logger log = LoggerFactory.getLogger(BatchUploadMapper.class);

  public static FileProcessingStatus getFileProcessingStatus(
      String fileName, UploadStatus uploadStatus, LocalDateTime processedOn) {
    return FileProcessingStatus.builder()
        .fileName(fileName)
        .status(uploadStatus.name())
        .processedOn(processedOn)
        .build();
  }

  public static Mono<BatchUploadRequest> validateBatchUploadRequest(
      BatchUploadRequest batchUploadRequest) {
    log.info("Validation Start :: {}", batchUploadRequest);
    if ((!batchUploadRequest.isLocalBatchUpload()
            && !StringUtils.isBlank(batchUploadRequest.getDirectoryPath()))
        || (batchUploadRequest.isLocalBatchUpload()
            && !StringUtils.isBlank(batchUploadRequest.getDirectoryPath()))) {
      return Mono.just(batchUploadRequest);
    }
    return Mono.error(new GenericException(ErrorMessage.MANDATORY_FIELDS_MISSING_OR_INVALID));
  }

  public static RefSet getRefSet(CSVRecord record) {
    try {
      return RefSet.builder()
          .id(UUID.fromString(record.get("ID")))
          .refSetId(Integer.parseInt(record.get("REF_SET_ID")))
          .refSetName(record.get("REF_SET_NAME"))
          .isActive(Boolean.parseBoolean(record.get("IS_ACTIVE")))
          .createdOn(LocalDateTime.parse(record.get("CREATED_ON").replace(" ", "T")))
          .createdBy(record.get("CREATED_BY"))
          .updatedOn(LocalDateTime.parse(record.get("UPDATED_ON").replace(" ", "T")))
          .updatedBy(record.get("UPDATED_BY"))
          .build();
    } catch (Exception e) {
      System.err.println("Error converting CSVRecord to RefSet: " + e.getMessage());
      return null; // Or handle this more gracefully
    }
  }

  public static RefTerm getRefTerm(CSVRecord record) {
    try {
      return RefTerm.builder()
          .id(UUID.fromString(record.get("ID")))
          .refTermId(Integer.parseInt(record.get("REF_TERM_ID")))
          .refSetId(UUID.fromString(record.get("REF_SET_ID")))
          .refTermName(record.get("REF_TERM_NAME"))
          .isActive(Boolean.parseBoolean(record.get("IS_ACTIVE")))
          .createdOn(LocalDateTime.parse(record.get("CREATED_ON").replace(" ", "T")))
          .createdBy(record.get("CREATED_BY"))
          .updatedOn(LocalDateTime.parse(record.get("UPDATED_ON").replace(" ", "T")))
          .updatedBy(record.get("UPDATED_BY"))
          .build();
    } catch (Exception e) {
      System.err.println("Error converting CSVRecord to RefSet: " + e.getMessage());
      return null; // Or handle this more gracefully
    }
  }
}
