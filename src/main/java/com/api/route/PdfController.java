package com.api.route;

import com.api.common.ApiResponse;
import com.api.pdf.GetCV;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PdfController {
  @CrossOrigin(origins = "http://localhost:3000")
  @GetMapping("/cv")
  public ResponseEntity<ApiResponse> getCV() {
    GetCV cv = new GetCV();
    return cv.getCV();
  }
}
