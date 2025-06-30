package com.api.pdf;

import com.api.common.ApiResponse;
import com.api.common.S3Connector;
import java.util.Map;
import org.springframework.http.ResponseEntity;

public class GetCV {
  private final S3Connector connector;

  public S3Connector getConnector() {
    return this.connector;
  }

  public GetCV() {
    connector = new S3Connector();
  }

  public ResponseEntity<ApiResponse> getCV() {
    String cdn =
        new StringBuilder()
            .append("https://d3bjrjf10s3vbi.cloudfront.net/")
            .append("CV.pdf")
            .toString();

    Map<String, String> map = Map.of("url", cdn);

    ApiResponse requestResponse = new ApiResponse(true, "pre-signed url for s3 upload", map);
    return ResponseEntity.ok(requestResponse);
  }
}
