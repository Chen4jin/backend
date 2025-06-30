package com.api.route;

import com.api.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HandShake {
  @CrossOrigin(origins = "http://localhost:3000")
  @GetMapping("/")
  public ResponseEntity<ApiResponse> handShake() {
    ApiResponse baseResponse = new ApiResponse(true, "Hand Shake.", "");

    return ResponseEntity.ok(baseResponse);
  }
}
