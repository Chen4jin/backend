package com.api.stats;

import com.api.common.ApiResponse;
import com.api.common.DynamoDBConnection;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;

public class Photo {
  private final DynamoDBConnection connector;
  private final String tableName = "tbl_photo";

  public DynamoDBConnection getConnector() {
    return this.connector;
  }

  public Photo() {
    connector = new DynamoDBConnection();
  }

  public ResponseEntity<ApiResponse> getPhoto() {

    try {
      DescribeTableResponse describeTableResponse =
          this.connector
              .getClient()
              .describeTable(DescribeTableRequest.builder().tableName(this.tableName).build());

      Map<String, String> data =
          Map.of("count", String.valueOf(describeTableResponse.table().itemCount()));
      ApiResponse response =
          new ApiResponse(
              "success",
              200,
              "The resource describing the result of the action is transmitted in the message"
                  + " body.",
              data,
              null);
      return ResponseEntity.ok(response);

    } catch (NullPointerException e) {
      ApiResponse response =
          new ApiResponse("error", 500, "Failed to init DynamoDbClient", "", e.getMessage());
      return ResponseEntity.internalServerError().body(response);
    }
  }
}
