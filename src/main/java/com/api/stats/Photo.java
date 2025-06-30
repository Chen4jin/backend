package com.api.stats;

import com.api.common.ApiResponse;
import com.api.common.DynamoDBConnection;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;

public class Photo {
  private final DynamoDBConnection connector;

  public DynamoDBConnection getConnector() {
    return this.connector;
  }

  public Photo() {
    connector = new DynamoDBConnection();
  }

  public ResponseEntity<ApiResponse> getPhoto() {
    String tableName = "tbl_photo";

    DescribeTableResponse response =
        this.connector
            .getClient()
            .describeTable(DescribeTableRequest.builder().tableName(tableName).build());

    Map<String, String> map = Map.of("count", String.valueOf(response.table().itemCount()));
    ApiResponse requestResponse =
        new ApiResponse(
            true,
            "The resource describing the result of the action is transmitted in the message body.",
            map);
    return ResponseEntity.ok(requestResponse);
  }
}
