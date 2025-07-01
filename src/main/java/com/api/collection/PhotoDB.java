package com.api.collection;

import com.api.common.ApiResponse;
import com.api.common.Constant;
import com.api.common.DynamoDBConnection;
import com.api.response.PhotoResponse;
import java.net.URLConnection;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
  private final String tableName = "tbl_photo";
  private final String s3Bucket = "photo-collection-jin/";

  public DynamoDBConnection getConnector() {
    return this.connector;
  }

  public PhotoDB() {
    connector = new DynamoDBConnection();
  }

  public ResponseEntity<PhotoResponse> getPhoto(String lastKey, Integer pageSize) {
    // Define which attributes to return in the scan results to minimize data
    // transferred
    String projectionExpression = "imageID, fileName, cloudFront";

    // Define a filter expression to include only items where 'isDeleted' is false
    String filterExpression = "isDeleted = :val";
    Map<String, AttributeValue> expressionAttributeValues =
        Map.of(":val", AttributeValue.builder().bool(false).build());

    // Build the ScanRequest with the following parameters:
    // - projectionExpression to limit returned attributes
    // - tableName to specify the DynamoDB table to scan
    // - filterExpression to apply a filter condition on the scan
    // - expressionAttributeValues to supply values for filter placeholders
    // - limit to restrict the number of items returned per scan page (pagination)
    ScanRequest.Builder scanBuilder =
        ScanRequest.builder()
            .projectionExpression(projectionExpression)
            .tableName(this.tableName)
            .filterExpression(filterExpression)
            .expressionAttributeValues(expressionAttributeValues)
            .limit(pageSize);

    // If a last evaluated key is provided (for pagination), set it as the exclusive
    // start key
    // This tells DynamoDB where to continue scanning from
    if (lastKey != null && !lastKey.isEmpty()) {
      Map<String, AttributeValue> exclusiveStartKey = new HashMap<>();
      exclusiveStartKey.put("imageID", AttributeValue.builder().s(lastKey).build());
      scanBuilder.exclusiveStartKey(exclusiveStartKey);
    }

    PhotoResponse requestResponse;

    try {
      // Execute the DynamoDB scan request built earlier, and get the response
      ScanResponse response = this.getConnector().getClient().scan(scanBuilder.build());

      // Retrieve the last evaluated key from the response for pagination
      // This key indicates where the next scan should continue, if present
      Map<String, AttributeValue> responseLastKey = response.lastEvaluatedKey();
      String imageID = "";
      if (responseLastKey != null && !responseLastKey.isEmpty()) {
        imageID = responseLastKey.get("imageID").s();
      }

      // Determine if there are more items to be scanned based on the presence of a
      // last evaluated key
      boolean hasMore = (responseLastKey != null && !responseLastKey.isEmpty());

      List<Map<String, Object>> responseData = new ArrayList<>();

      // Iterate over each item returned by the scan (each item is a Map<String,
      // AttributeValue>)

      for (Map<String, AttributeValue> item : response.items()) {
        // For each attribute in the item, extract its string value and put into the
        // simplified map
        Map<String, Object> newItem = new HashMap<>();
        for (Map.Entry<String, AttributeValue> entry : item.entrySet()) {
          newItem.put(entry.getKey(), entry.getValue().s());
        }
        responseData.add(newItem);
      }

      ApiResponse base =
          new ApiResponse(
              "success",
              200,
              "The resource has been fetched and transmitted in the message body.",
              responseData,
              null);
      requestResponse = new PhotoResponse(base, imageID, hasMore);

      return ResponseEntity.ok(requestResponse);

    } catch (NullPointerException e) {
      String[] responseData = new String[0];
      ApiResponse base =
          new ApiResponse(
              "error", 500, "Failed to init DynamoDbClient", responseData, e.getMessage());
      requestResponse = new PhotoResponse(base, "", false);
      return ResponseEntity.internalServerError().body(requestResponse);
    }
  }

  public ResponseEntity<ApiResponse> postPhoto(String imageID, String fileName, String sizeBytes) {
    // Construct the S3 key by concatenating bucket name and image ID
    String s3Key = new StringBuilder().append(s3Bucket).append(imageID).toString();

    // Construct the CloudFront CDN URL by concatenating CloudFront base URL and
    // image ID
    String cdn = new StringBuilder().append(Constant.getCloudFront()).append(imageID).toString();

    // Guess content type (MIME type) from the filename extension
    String contentType = URLConnection.guessContentTypeFromName(fileName);

    // Get the current UTC timestamp formatted as ISO 8601 string (without
    // milliseconds)
    String now =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now());

    // Create a map representing the DynamoDB item attributes
    Map<String, AttributeValue> item = new HashMap<>();
    item.put("imageID", AttributeValue.builder().s(imageID).build());
    item.put("fileName", AttributeValue.builder().s(fileName).build());
    item.put("s3Key", AttributeValue.builder().s(s3Key).build());
    item.put("contentType", AttributeValue.builder().s(contentType).build());
    item.put("createdAt", AttributeValue.builder().s(now).build());
    item.put("sizeBytes", AttributeValue.builder().s(sizeBytes).build());
    item.put("isDeleted", AttributeValue.builder().bool(false).build());
    item.put("cloudFront", AttributeValue.builder().s(cdn).build());

    // Define a condition expression to ensure no existing item has the same imageID
    String conditionExpression =
        new StringBuilder().append("attribute_not_exists(imageID)").toString();

    // Build the PutItemRequest with condition to prevent overwriting existing item
    PutItemRequest putRequest =
        PutItemRequest.builder()
            .tableName(this.tableName)
            .item(item)
            .conditionExpression(conditionExpression)
            .build();

    try {
      // Attempt to insert the item into DynamoDB only if imageID doesn't exist
      this.getConnector().getClient().putItem(putRequest);
    } catch (NullPointerException e) {
      // NullPointerException might happen if any variable is null (consider better
      // checks)

      ApiResponse response =
          new ApiResponse("error", 500, "Failed to init DynamoDbClient", "", e.getMessage());
      return ResponseEntity.internalServerError().body(response);
    } catch (ConditionalCheckFailedException e) {
      // ConditionalCheckFailedException occurs if imageID already exists (insert
      // rejected)

      ApiResponse response =
          new ApiResponse("error", 500, "item already exist", "", e.getMessage());
      return ResponseEntity.internalServerError().body(response);
    }

    ApiResponse requestResponse =
        new ApiResponse(
            "success", 200, "S3 image metadata synced successfully in the database", "", null);
    return ResponseEntity.ok(requestResponse);
  }
}
