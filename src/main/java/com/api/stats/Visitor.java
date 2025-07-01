package com.api.stats;

import com.api.common.ApiResponse;
import com.api.common.DynamoDBConnection;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

public class Visitor {
  private static final Logger logger = LoggerFactory.getLogger(Visitor.class);
  private final DynamoDBConnection connector;
  private final String tableName = "tbl_visitor";

  public DynamoDBConnection getConnector() {
    return this.connector;
  }

  public Visitor() {
    connector = new DynamoDBConnection();
  }

  public Map<String, AttributeValue> primaryKey() {
    LocalDate today = LocalDate.now();
    int year = today.getYear();
    int month = today.getMonthValue();

    Map<String, AttributeValue> key = new HashMap<>();
    key.put("year", AttributeValue.builder().n(Integer.toString(year)).build());
    key.put("month", AttributeValue.builder().n(Integer.toString(month)).build());
    return key;
  }

  public ResponseEntity<ApiResponse> getVisitor() {

    GetItemRequest getItemRequest = GetItemRequest.builder().tableName(this.tableName).key(this.primaryKey()).build();
    ApiResponse response;
    try {
      GetItemResponse getItemResponse = this.getConnector().getClient().getItem(getItemRequest);
      if (getItemResponse.hasItem()) {
        Map<String, String> data = Map.of("count", getItemResponse.item().get("count").n());
        response = new ApiResponse(
            "success", 200, "The resource has been fetched and transmitted in the message body.", data, null);
      } else {
        this.putVisitor();
        Map<String, String> data = Map.of("count", "1");
        response = new ApiResponse(
            "success", 200, "No existing item was found; a new item has been successfully created.", data, null);
      }
      return ResponseEntity.ok(response);

    } catch (NullPointerException e) {
      response = new ApiResponse("error", 500, "AWS SDK client initialization failed.", "", e.getMessage());
      return ResponseEntity.internalServerError().body(response);
    }
  }

  public ResponseEntity<ApiResponse> postVisitor() {

    String updateExpression = new StringBuilder().append("SET #count = if_not_exists(#count, :start) + :inc")
        .toString();

    Map<String, String> expressionNames = new HashMap<>();
    expressionNames.put("#count", "count");

    Map<String, AttributeValue> expressionValues = new HashMap<>();
    expressionValues.put(":start", AttributeValue.builder().n("0").build());
    expressionValues.put(":inc", AttributeValue.builder().n("1").build());

    UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
        .tableName(this.tableName)
        .key(this.primaryKey())
        .updateExpression(updateExpression)
        .expressionAttributeNames(expressionNames)
        .expressionAttributeValues(expressionValues)
        .returnValues(ReturnValue.ALL_NEW)
        .build();
    ApiResponse response;

    try {
      UpdateItemResponse updateItemResponse = this.getConnector().getClient().updateItem(updateItemRequest);
      Map<String, String> data;
      if (updateItemResponse.hasAttributes()) {
        data = Map.of("count", updateItemResponse.attributes().get("count").n());
        response = new ApiResponse(
            "success",
            200,
            "The resource describing the result of the action is transmitted in the message body.",
            data,
            null);
        return ResponseEntity.ok(response);
      } else {
        data = Map.of("count", "1");
        response = new ApiResponse("error", 503, "Failed to execute UpdateItem request on DynamoDB.", data, null);
        return ResponseEntity.internalServerError().body(response);
      }

    } catch (NullPointerException e) {
      response = new ApiResponse("error", 500, "AWS SDK client initialization failed.", "", e.getMessage());
      return ResponseEntity.internalServerError().body(response);
    }
  }

  public void putVisitor() {

    Map<String, AttributeValue> item = this.primaryKey();

    item.put("count", AttributeValue.builder().n("1").build());

    String conditionExpression = new StringBuilder()
        .append("attribute_not_exists(#year) AND attribute_not_exists(#month)")
        .toString();

    Map<String, String> expressionNames = new HashMap<>();
    expressionNames.put("#year", "year");
    expressionNames.put("#month", "month");

    PutItemRequest putRequest = PutItemRequest.builder()
        .tableName(this.tableName)
        .item(item)
        .conditionExpression(conditionExpression)
        .expressionAttributeNames(expressionNames)
        .build();
    try {
      this.getConnector().getClient().putItem(putRequest);
    } catch (NullPointerException e) {
      logger.error("AWS SDK client initialization failed.");
    } catch (ConditionalCheckFailedException e) {
      // ConditionalCheckFailedException occurs if visitor already exists (insert
      // rejected)
      logger.error("visitor record already existed");
    }
  }
}
