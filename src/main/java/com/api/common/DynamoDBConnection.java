package com.api.common;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoDBConnection {
  private final DynamoDbClient client;

  public DynamoDbClient getClient() {
    return this.client;
  }

  public DynamoDBConnection() {
    Region region = Region.US_EAST_1;
    DynamoDbClient dynamoDbClient = null;
    try {
      dynamoDbClient = DynamoDbClient.builder().region(region).build();
    } catch (SdkClientException e) {

    }
    this.client = dynamoDbClient;
  }
}
