package com.api.config;

import com.api.common.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;

/**
 * Global exception handler for centralized error handling across all controllers.
 * Converts exceptions to consistent ApiResponse format.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /**
   * Handles validation errors from @Valid annotations.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse> handleValidationException(MethodArgumentNotValidException ex) {
    String errorMessage =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .reduce((a, b) -> a + "; " + b)
            .orElse("Validation failed");

    logger.warn("Validation error: {}", errorMessage);

    ApiResponse response =
        new ApiResponse("error", HttpStatus.BAD_REQUEST.value(), "Validation failed", null, errorMessage);
    return ResponseEntity.badRequest().body(response);
  }

  /**
   * Handles constraint violation errors.
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse> handleConstraintViolation(ConstraintViolationException ex) {
    String errorMessage =
        ex.getConstraintViolations().stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .reduce((a, b) -> a + "; " + b)
            .orElse("Constraint violation");

    logger.warn("Constraint violation: {}", errorMessage);

    ApiResponse response =
        new ApiResponse("error", HttpStatus.BAD_REQUEST.value(), "Validation failed", null, errorMessage);
    return ResponseEntity.badRequest().body(response);
  }

  /**
   * Handles AWS service exceptions.
   */
  @ExceptionHandler(AwsServiceException.class)
  public ResponseEntity<ApiResponse> handleAwsServiceException(AwsServiceException ex) {
    logger.error("AWS service error: {}", ex.getMessage(), ex);

    ApiResponse response =
        new ApiResponse(
            "error",
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            "AWS service error",
            null,
            ex.getMessage());
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
  }

  /**
   * Handles AWS SDK client exceptions (network errors, etc).
   */
  @ExceptionHandler(SdkClientException.class)
  public ResponseEntity<ApiResponse> handleSdkClientException(SdkClientException ex) {
    logger.error("AWS SDK client error: {}", ex.getMessage(), ex);

    ApiResponse response =
        new ApiResponse(
            "error",
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            "AWS connection error",
            null,
            ex.getMessage());
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
  }

  /**
   * Handles illegal argument exceptions.
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse> handleIllegalArgument(IllegalArgumentException ex) {
    logger.warn("Invalid argument: {}", ex.getMessage());

    ApiResponse response =
        new ApiResponse(
            "error", HttpStatus.BAD_REQUEST.value(), "Invalid request", null, ex.getMessage());
    return ResponseEntity.badRequest().body(response);
  }

  /**
   * Handles all other unhandled exceptions.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse> handleGenericException(Exception ex) {
    logger.error("Unexpected error: {}", ex.getMessage(), ex);

    ApiResponse response =
        new ApiResponse(
            "error",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred",
            null,
            ex.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
