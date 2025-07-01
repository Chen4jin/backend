package com.api.routes;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.common.ApiResponse;

@RestController
public class HandShake {
  @GetMapping("/")
  public ResponseEntity<ApiResponse> handShake() {
    ApiResponse response = new ApiResponse("success", 200, "Handshake completed successfully.", "", null);

    return ResponseEntity.ok(response);
  }
}
