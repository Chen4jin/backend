package com.api.routes;

import com.api.common.ApiResponse;
import com.api.pdf.CV;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class PdfController {
  @GetMapping("/cv")
  public ResponseEntity<ApiResponse> getCV() {
    CV cv = new CV();
    return cv.getCV();
  }
}
