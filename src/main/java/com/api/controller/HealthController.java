package com.api.controller;

import com.api.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for health check endpoints.
 */
@RestController
public class HealthController {

  /**
   * Health check / handshake endpoint.
   *
   * @return success response indicating the service is healthy
   */
  @GetMapping("/")
  public ResponseEntity<ApiResponse> healthCheck() {
    ApiResponse response =
        new ApiResponse("success", 200, "Handshake completed successfully.", null, null);
    return ResponseEntity.ok(response);
  }
}
