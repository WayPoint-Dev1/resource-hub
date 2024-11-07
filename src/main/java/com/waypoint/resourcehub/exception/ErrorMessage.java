package com.waypoint.resourcehub.exception;

import lombok.Getter;

@Getter
public enum ErrorMessage {
  MANDATORY_FIELDS_MISSING_OR_INVALID(
      422, "MANDATORY_FIELDS_MISSING_OR_INVALID", "MANDATORY FIELDS MISSING/INVALID IN PAYLOAD"),
  INVALID_ASSET_TYPE_ID(422, "INVALID_ASSET_TYPE_ID", "PROVIDED ASSET_TYPE_ID IS NOT VALID"),
  S3_UPLOAD_FAILED(500, "S3_UPLOAD_FAILED", "ERROR WHILE UPLOADING FILE TO S3"),
  S3_DOWNLOAD_FAILED(500, "S3_DOWNLOAD_FAILED", "ERROR WHILE DOWNLOADING FILE FROM S3"),
  S3_RETRIEVE_FAILED(500, "S3_GET_FAILED", "ERROR WHILE RETRIEVING FILE FROM S3"),
  FAILED_TO_READ_BATCH_FILE(500, "FAILED_TO_READ_BATCH_FILE", "FAILED TO READ BATCH FILE"),
  S3_CLEANUP_FAILED(500, "S3_CLEANUP_FAILED", "ERROR WHILE DELETING FILE FROM S3"),
  ASSET_SAVE_FAILED(
      500,
      "ASSET_SAVE_FAILED",
      "ASSET SUCCESSFULLY UPLOADED TO S3, BUT ENCOUNTERED AN ERROR WHILE SAVING THE ASSET PATH"),
  ASSET_NOT_FOUND(422, "ASSET_NOT_FOUND", "REQUESTED ASSET IS NOT AVAILABLE"),
  ASSET_RETRIEVAL_FROM_S3_FAILED(500, "ASSET_RETRIEVAL_FROM_S3_FAILED", "ASSET RETRIEVAL FROM S3 FAILED"),
  BATCH_FILES_NOT_READABLE(422, "BATCH_FILES_NOT_READABLE", "ERROR WHILE READING BATCH FILES"),
  UNAUTHORIZED(401, "UNAUTHORIZED", "UNAUTHORIZED");
  private final int httpStatusCode;
  private final String messageCode;
  private final String messageDescription;

  ErrorMessage(int httpStatusCode, String messageCode, String messageDescription) {
    this.httpStatusCode = httpStatusCode;
    this.messageCode = messageCode;
    this.messageDescription = messageDescription;
  }
}
