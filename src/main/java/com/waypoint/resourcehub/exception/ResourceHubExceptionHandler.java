package com.waypoint.resourcehub.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class ResourceHubExceptionHandler {

  @ExceptionHandler(GenericException.class)
  public Mono<ResponseEntity<Map<String, String>>> handleAuthException(GenericException ex) {
    Map<String, String> errorMap = new HashMap<>();
    errorMap.put("timestamp", LocalDateTime.now().toString());
    errorMap.put("status", String.valueOf(ex.getErrorMessage().getHttpStatusCode()));
    errorMap.put("error", ex.getErrorMessage().getMessageCode());
    errorMap.put("message", ex.getErrorMessage().getMessageDescription());
    return Mono.just(
        ResponseEntity.status(ex.getErrorMessage().getHttpStatusCode()).body(errorMap));
  }
}
