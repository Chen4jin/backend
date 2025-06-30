package com.api.stats;

import com.api.common.ApiResponse;
import com.api.common.DynamoDBConnection;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
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
  private final DynamoDBConnection connector;

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
    String tableName = "tbl_visitor";

    GetItemRequest getItemRequest =
        GetItemRequest.builder().tableName(tableName).key(this.primaryKey()).build();
    ApiResponse requestResponse;
    try {
      GetItemResponse response = this.getConnector().getClient().getItem(getItemRequest);
      if (response.hasItem()) {
        Map<String, String> map = Map.of("count", response.item().get("count").n());
        requestResponse =
            new ApiResponse(
                true, "The resource has been fetched and transmitted in the message body.", map);
      } else {
        this.putVisitor();
        Map<String, String> map = Map.of("count", "1");
        requestResponse =
            new ApiResponse(
                true, "No existing item was found; a new item has been successfully created.", map);
      }
      return ResponseEntity.ok(requestResponse);

    } catch (NullPointerException e) {
      requestResponse = new ApiResponse(false, "AWS SDK client initialization failed.", "");
      return ResponseEntity.internalServerError().body(requestResponse);
    }
  }

  public ResponseEntity<ApiResponse> postVisitor() {
    String tableName = "tbl_visitor";

    String updateExpression =
        new StringBuilder().append("SET #count = if_not_exists(#count, :start) + :inc").toString();

    Map<String, String> expressionNames = new HashMap<>();
    expressionNames.put("#count", "count");

    Map<String, AttributeValue> expressionValues = new HashMap<>();
    expressionValues.put(":start", AttributeValue.builder().n("0").build());
    expressionValues.put(":inc", AttributeValue.builder().n("1").build());

    UpdateItemRequest updateItemRequest =
        UpdateItemRequest.builder()
            .tableName(tableName)
            .key(this.primaryKey())
            .updateExpression(updateExpression)
            .expressionAttributeNames(expressionNames)
            .expressionAttributeValues(expressionValues)
            .returnValues(ReturnValue.ALL_NEW)
            .build();
    ApiResponse requestResponse;

    try {
      UpdateItemResponse response = this.getConnector().getClient().updateItem(updateItemRequest);
      if (response.hasAttributes()) {
        Map<String, String> map = Map.of("count", response.attributes().get("count").n());
        requestResponse =
            new ApiResponse(
                true,
                "The resource describing the result of the action is transmitted in the message"
                    + " body.",
                map);
        return ResponseEntity.ok(requestResponse);
      } else {
        Map<String, String> map = Map.of("count", "1");
        requestResponse =
            new ApiResponse(true, "Failed to execute UpdateItem request on DynamoDB.", map);
        return ResponseEntity.badRequest().body(requestResponse);
      }

    } catch (NullPointerException e) {
      requestResponse = new ApiResponse(false, "AWS SDK client initialization failed.", "");
      return ResponseEntity.internalServerError().body(requestResponse);
    }
  }

  public void putVisitor() {
    String tableName = "tbl_visitor";

    Map<String, AttributeValue> item = this.primaryKey();

    item.put("count", AttributeValue.builder().n("1").build());

    String conditionExpression =
        new StringBuilder()
            .append("attribute_not_exists(#year) AND attribute_not_exists(#month)")
            .toString();

    Map<String, String> expressionNames = new HashMap<>();
    expressionNames.put("#year", "year");
    expressionNames.put("#month", "month");

    PutItemRequest putRequest =
        PutItemRequest.builder()
            .tableName(tableName)
            .item(item)
            .conditionExpression(conditionExpression)
            .expressionAttributeNames(expressionNames)
            .build();
    try {
      this.getConnector().getClient().putItem(putRequest);
    } catch (NullPointerException | ConditionalCheckFailedException e) {
    }
  }
}
