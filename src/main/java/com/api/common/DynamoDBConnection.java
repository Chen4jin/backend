package com.api.common;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamoDBConnection {
  private final DynamoDbClient client;
  private static final Logger logger = LoggerFactory.getLogger(DynamoDBConnection.class);

  public DynamoDbClient getClient() {
    return this.client;
  }

  public DynamoDBConnection() {
    DynamoDbClient dynamoDbClient = null;

    try {
      dynamoDbClient = DynamoDbClient.builder().region(Constant.getRegion()).build();
    } catch (SdkClientException e) {
      // Handle client-side exceptions like network errors
      logger.error("Failed to init DynamoDbClient due to network errors!");
    } catch (AwsServiceException e) {
      // Handle service-side exceptions returned from AWS
      logger.error("Failed to init DynamoDbClient due to service-side exceptions returned from AWS!");
    } catch (Exception e) {
      // Catch any other unexpected exceptions
      logger.error("unexpected exceptions {}!", e.getMessage(), e);
    }
    this.client = dynamoDbClient;
  }
}
