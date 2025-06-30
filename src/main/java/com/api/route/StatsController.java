package com.api.route;

import com.api.common.ApiResponse;
import com.api.stats.Photo;
import com.api.stats.Visitor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatsController {
  @CrossOrigin(origins = "http://localhost:3000")
  @GetMapping("/visitor")
  public ResponseEntity<ApiResponse> getVisitor() {
    Visitor stats = new Visitor();
    return stats.getVisitor();
  }

  @PostMapping("/visitor")
  public ResponseEntity<ApiResponse> postVisitor() {
    Visitor stats = new Visitor();
    return stats.postVisitor();
  }

  @CrossOrigin(origins = "http://localhost:3000")
  @GetMapping("/photoStats")
  public ResponseEntity<ApiResponse> photoStats() {
    Photo stats = new Photo();
    return stats.getPhoto();
  }
}
