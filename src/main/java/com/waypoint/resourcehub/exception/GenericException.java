package com.waypoint.resourcehub.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
public class GenericException extends RuntimeException {
  private final ErrorMessage errorMessage;

  @Builder
  public GenericException(ErrorMessage errorMessage) {
    super(errorMessage.getMessageDescription());
    this.errorMessage = errorMessage;
  }
}
