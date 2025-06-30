package com.api.collection;

import com.api.common.ApiResponse;
import com.api.common.DynamoDBConnection;
import com.api.response.PhotoResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

public class PhotoDB {
  private final DynamoDBConnection connector;

  public DynamoDBConnection getConnector() {
    return this.connector;
  }

  public PhotoDB() {
    connector = new DynamoDBConnection();
  }

  public ResponseEntity<PhotoResponse> getPhoto(String lastKey, Integer pageSize) {
    String tableName = "tbl_photo";
    String projectionExpression = "imageID, fileName, cloudFront";
    String filterExpression = "isDeleted = :val";
    Map<String, AttributeValue> expressionAttributeValues =
        Map.of(":val", AttributeValue.builder().bool(false).build());

    ScanRequest.Builder scanBuilder =
        ScanRequest.builder()
            .projectionExpression(projectionExpression)
            .tableName(tableName)
            .filterExpression(filterExpression)
            .expressionAttributeValues(expressionAttributeValues)
            .limit(pageSize);

    if (lastKey != null && !lastKey.isEmpty()) {
      Map<String, AttributeValue> exclusiveStartKey = new HashMap<>();
      exclusiveStartKey.put("imageID", AttributeValue.builder().s(lastKey).build());
      scanBuilder.exclusiveStartKey(exclusiveStartKey);
    }

    PhotoResponse requestResponse;
    try {
      ScanResponse response = this.getConnector().getClient().scan(scanBuilder.build());
      Map<String, AttributeValue> responseLastKey = response.lastEvaluatedKey();
      String imageID = "";
      if (responseLastKey != null && !responseLastKey.isEmpty()) {
        imageID = responseLastKey.get("imageID").s(); // .s() gets the String value
      }

      boolean hasMore = (responseLastKey != null && !responseLastKey.isEmpty());

      List<Map<String, Object>> simpleItems = new ArrayList<>();

      for (Map<String, AttributeValue> item : response.items()) {

        Map<String, Object> newItem = new HashMap<>();
        for (Map.Entry<String, AttributeValue> entry : item.entrySet()) {
          newItem.put(entry.getKey(), entry.getValue().s());
        }
        simpleItems.add(newItem);
      }
      ApiResponse baseResponse =
          new ApiResponse(
              true,
              "The resource has been fetched and transmitted in the message body.",
              simpleItems);
      requestResponse = new PhotoResponse(baseResponse, imageID, hasMore);

      return ResponseEntity.ok(requestResponse);

    } catch (NullPointerException e) {
      ApiResponse baseResponse =
          new ApiResponse(false, "AWS SDK client initialization failed.", "");
      requestResponse = new PhotoResponse(baseResponse, "", false);
      return ResponseEntity.internalServerError().body(requestResponse);
    }
  }

  public ResponseEntity<ApiResponse> postPhoto(String imageID, String fileName, String sizeBytes) {
    String tableName = "tbl_photo";
    String s3Key = new StringBuilder().append("photo-collection-jin/").append(imageID).toString();
    String cdn =
        new StringBuilder()
            .append("https://d3bjrjf10s3vbi.cloudfront.net/")
            .append(imageID)
            .toString();

    Map<String, AttributeValue> item = new HashMap<>();
    item.put("imageID", AttributeValue.builder().s(imageID).build());
    item.put("fileName", AttributeValue.builder().s(fileName).build());
    item.put("s3Key", AttributeValue.builder().s(s3Key).build());
    item.put("contentType", AttributeValue.builder().s("image/png").build());
    item.put("createdAt", AttributeValue.builder().s("2025-06-02T13:05:00Z").build());
    item.put("sizeBytes", AttributeValue.builder().s(sizeBytes).build());
    item.put("isDeleted", AttributeValue.builder().bool(false).build());
    item.put("cloudFront", AttributeValue.builder().s(cdn).build());

    String conditionExpression =
        new StringBuilder().append("attribute_not_exists(imageID)").toString();

    PutItemRequest putRequest =
        PutItemRequest.builder()
            .tableName(tableName)
            .item(item)
            .conditionExpression(conditionExpression)
            .build();
    try {
      this.getConnector().getClient().putItem(putRequest);
    } catch (NullPointerException | ConditionalCheckFailedException e) {
    }

    Map<String, String> map = Map.of("url", "test");

    ApiResponse requestResponse = new ApiResponse(true, "pre-signed url for s3 upload", map);
    return ResponseEntity.ok(requestResponse);
  }
}
