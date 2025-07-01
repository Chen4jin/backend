package com.api.pdf;

import com.api.common.ApiResponse;
import com.api.common.Constant;
import com.api.common.S3Connector;
import java.util.Map;
import org.springframework.http.ResponseEntity;

public class CV {
  private final S3Connector connector;
  private final String resume = "CV.pdf";

  public S3Connector getConnector() {
    return this.connector;
  }

  public CV() {
    connector = new S3Connector();
  }

  public ResponseEntity<ApiResponse> getCV() {
    String cdn = new StringBuilder().append(Constant.getCloudFront()).append(resume).toString();

    Map<String, String> map = Map.of("url", cdn);

    ApiResponse response =
        new ApiResponse("success", 200, "CloudFront-hosted URL for CV download", map, null);
    return ResponseEntity.ok(response);
  }
}
