package com.api.service.impl;

import com.api.common.ApiResponse;
import com.api.common.Constant;
import com.api.config.AwsProperties;
import com.api.dto.response.PhotoResponse;
import com.api.service.PhotoService;
import com.api.util.DateTimeUtil;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

/**
 * Implementation of PhotoService.
 * Handles photo operations using DynamoDB and S3.
 */
@Service
public class PhotoServiceImpl implements PhotoService {

  private static final Logger logger = LoggerFactory.getLogger(PhotoServiceImpl.class);

  private static final Duration PRESIGNED_URL_DURATION = Duration.ofMinutes(2);
  private static final int NANO_ID_LENGTH = 8;

  // DynamoDB attribute names
  private static final String ATTR_IMAGE_ID = "imageID";
  private static final String ATTR_FILE_NAME = "fileName";
  private static final String ATTR_CLOUD_FRONT = "cloudFront";
  private static final String ATTR_IS_DELETED = "isDeleted";

  private final DynamoDbClient dynamoDbClient;
  private final S3Presigner s3Presigner;
  private final AwsProperties awsProperties;

  public PhotoServiceImpl(DynamoDbClient dynamoDbClient, S3Presigner s3Presigner, AwsProperties awsProperties) {
    this.dynamoDbClient = dynamoDbClient;
    this.s3Presigner = s3Presigner;
    this.awsProperties = awsProperties;
  }

  private static final int DEFAULT_PAGE_SIZE = 10;
  private static final int MAX_PAGE_SIZE = 100;

  @Override
  public PhotoResponse getPhotos(String lastKey, Integer pageSize) {
    // Validate and sanitize pageSize
    int validPageSize = (pageSize == null || pageSize <= 0) ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
    logger.debug("Fetching photos with lastKey={}, pageSize={}", lastKey, validPageSize);

    String projectionExpression = String.join(", ", ATTR_IMAGE_ID, ATTR_FILE_NAME, ATTR_CLOUD_FRONT);
    String filterExpression = ATTR_IS_DELETED + " = :val";
    Map<String, AttributeValue> expressionAttributeValues =
        Map.of(":val", AttributeValue.builder().bool(false).build());

    ScanRequest.Builder scanBuilder =
        ScanRequest.builder()
            .projectionExpression(projectionExpression)
            .tableName(awsProperties.getPhotoTable())
            .filterExpression(filterExpression)
            .expressionAttributeValues(expressionAttributeValues)
            .limit(validPageSize);

    if (lastKey != null && !lastKey.isEmpty()) {
      Map<String, AttributeValue> exclusiveStartKey = new HashMap<>();
      exclusiveStartKey.put(ATTR_IMAGE_ID, AttributeValue.builder().s(lastKey).build());
      scanBuilder.exclusiveStartKey(exclusiveStartKey);
    }

    ScanResponse response = dynamoDbClient.scan(scanBuilder.build());

    Map<String, AttributeValue> responseLastKey = response.lastEvaluatedKey();
    String nextKey = "";
    boolean hasMore = false;

    // Safe null check before accessing lastEvaluatedKey
    if (responseLastKey != null && !responseLastKey.isEmpty()) {
      AttributeValue imageIdValue = responseLastKey.get(ATTR_IMAGE_ID);
      if (imageIdValue != null && imageIdValue.s() != null) {
        nextKey = imageIdValue.s();
        hasMore = true;
      }
    }

    List<Map<String, Object>> responseData = new ArrayList<>();
    for (Map<String, AttributeValue> item : response.items()) {
      Map<String, Object> newItem = new HashMap<>();
      for (Map.Entry<String, AttributeValue> entry : item.entrySet()) {
        AttributeValue value = entry.getValue();
        // Handle different AttributeValue types safely
        if (value.s() != null) {
          newItem.put(entry.getKey(), value.s());
        } else if (value.n() != null) {
          newItem.put(entry.getKey(), value.n());
        } else if (value.bool() != null) {
          newItem.put(entry.getKey(), value.bool());
        }
      }
      responseData.add(newItem);
    }

    logger.info("Retrieved {} photos, hasMore={}", responseData.size(), hasMore);

    ApiResponse base =
        new ApiResponse(
            "success",
            200,
            "The resource has been fetched and transmitted in the message body.",
            responseData,
            null);

    return new PhotoResponse(base, nextKey, hasMore);
  }

  @Override
  public ApiResponse generateUploadUrl(String contentType) {
    logger.debug("Generating upload URL for contentType={}", contentType);

    // Validate contentType
    if (contentType == null || contentType.isBlank()) {
      throw new IllegalArgumentException("Content type is required");
    }
    if (!Constant.getMimeToExt().containsKey(contentType)) {
      throw new IllegalArgumentException(
          "Invalid content type. Allowed: " + Constant.getMimeToExt().keySet());
    }

    String uuid =
        NanoIdUtils.randomNanoId(
            NanoIdUtils.DEFAULT_NUMBER_GENERATOR, NanoIdUtils.DEFAULT_ALPHABET, NANO_ID_LENGTH);

    String date = DateTimeUtil.getCurrentDateCompact();
    String ext = Constant.getMimeToExt().get(contentType);
    String fileName = uuid + date + ext;

    PutObjectRequest putObjectRequest =
        PutObjectRequest.builder()
            .bucket(awsProperties.getPhotosBucket())
            .key(fileName)
            .contentType(contentType)
            .build();

    PresignedPutObjectRequest presignedRequest =
        s3Presigner.presignPutObject(
            presignBuilder ->
                presignBuilder
                    .signatureDuration(PRESIGNED_URL_DURATION)
                    .putObjectRequest(putObjectRequest));

    URL url = presignedRequest.url();

    logger.info("Generated presigned URL for image: {}", fileName);

    Map<String, String> data = Map.of("url", url.toString(), "imageID", fileName);
    return new ApiResponse("success", 200, "Pre-signed URL for S3 upload", data, null);
  }

  @Override
  public ApiResponse savePhotoMetadata(String imageId, String fileName, String sizeBytes) {
    logger.debug("Saving photo metadata for imageId={}", imageId);

    // S3 key is just the imageId (file name), not bucket/imageId
    String s3Key = imageId;
    String cdn = awsProperties.getPhotosCloudFront() + imageId;
    String contentType = URLConnection.guessContentTypeFromName(fileName);
    String now = DateTimeUtil.getCurrentTimestamp();

    Map<String, AttributeValue> item = new HashMap<>();
    item.put(ATTR_IMAGE_ID, AttributeValue.builder().s(imageId).build());
    item.put(ATTR_FILE_NAME, AttributeValue.builder().s(fileName).build());
    item.put("s3Key", AttributeValue.builder().s(s3Key).build());
    item.put("contentType", AttributeValue.builder().s(contentType).build());
    item.put("createdAt", AttributeValue.builder().s(now).build());
    item.put("sizeBytes", AttributeValue.builder().s(sizeBytes).build());
    item.put(ATTR_IS_DELETED, AttributeValue.builder().bool(false).build());
    item.put(ATTR_CLOUD_FRONT, AttributeValue.builder().s(cdn).build());

    PutItemRequest putRequest =
        PutItemRequest.builder()
            .tableName(awsProperties.getPhotoTable())
            .item(item)
            .conditionExpression("attribute_not_exists(" + ATTR_IMAGE_ID + ")")
            .build();

    try {
      dynamoDbClient.putItem(putRequest);
      logger.info("Saved photo metadata for imageId={}", imageId);
      return new ApiResponse(
          "success", 200, "S3 image metadata synced successfully in the database", null, null);
    } catch (ConditionalCheckFailedException e) {
      logger.warn("Photo already exists: imageId={}", imageId);
      return new ApiResponse("error", 409, "Image already exists", null, e.getMessage());
    }
  }

  @Override
  public ApiResponse deletePhoto(String imageId) {
    logger.debug("Deleting photo with imageId={}", imageId);

    if (imageId == null || imageId.isBlank()) {
      throw new IllegalArgumentException("Image ID is required");
    }

    Map<String, AttributeValue> key = new HashMap<>();
    key.put(ATTR_IMAGE_ID, AttributeValue.builder().s(imageId).build());

    Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
    expressionAttributeValues.put(":deleted", AttributeValue.builder().bool(true).build());

    UpdateItemRequest updateRequest =
        UpdateItemRequest.builder()
            .tableName(awsProperties.getPhotoTable())
            .key(key)
            .updateExpression("SET " + ATTR_IS_DELETED + " = :deleted")
            .expressionAttributeValues(expressionAttributeValues)
            .conditionExpression("attribute_exists(" + ATTR_IMAGE_ID + ")")
            .build();

    try {
      dynamoDbClient.updateItem(updateRequest);
      logger.info("Deleted photo with imageId={}", imageId);
      return new ApiResponse("success", 200, "Photo deleted successfully", null, null);
    } catch (ConditionalCheckFailedException e) {
      logger.warn("Photo not found: imageId={}", imageId);
      return new ApiResponse("error", 404, "Photo not found", null, e.getMessage());
    }
  }
}
