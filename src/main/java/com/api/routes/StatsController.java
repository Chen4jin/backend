package com.api.routes;

import com.api.common.ApiResponse;
import com.api.stats.Photo;
import com.api.stats.Visitor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/stats")
public class StatsController {
  @GetMapping("/visitors")
  public ResponseEntity<ApiResponse> getVisitor() {
    Visitor stats = new Visitor();
    return stats.getVisitor();
  }

  @PostMapping("/visitors")
  public ResponseEntity<ApiResponse> postVisitor() {
    Visitor stats = new Visitor();
    return stats.postVisitor();
  }

  @GetMapping("/photos")
  public ResponseEntity<ApiResponse> photoStats() {
    Photo stats = new Photo();
    return stats.getPhoto();
  }
}
