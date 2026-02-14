package com.api.service.impl;

import com.api.common.ApiResponse;
import com.api.common.Constant;
import com.api.config.AwsProperties;
import com.api.dto.request.SiteMessageRequest;
import com.api.dto.request.SocialLinksRequest;
import com.api.service.ProfileService;
import com.api.util.DateTimeUtil;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

/**
 * Implementation of ProfileService.
 * Handles profile operations using DynamoDB and S3.
 */
@Service
public class ProfileServiceImpl implements ProfileService {

  private static final Logger logger = LoggerFactory.getLogger(ProfileServiceImpl.class);

  private static final Duration PRESIGNED_URL_DURATION = Duration.ofMinutes(5);
  private static final String PROFILE_ID = "main";
  private static final int MAX_MESSAGE_LENGTH = 1000;
  private static final String STATUS_SUCCESS = "success";

  // DynamoDB attribute names
  private static final String ATTR_PROFILE_ID = "profileId";
  private static final String ATTR_GITHUB = "github";
  private static final String ATTR_LINKEDIN = "linkedin";
  private static final String ATTR_UPDATED_AT = "updatedAt";
  private static final String ATTR_SITE_MESSAGE = "siteMessage";

  private final DynamoDbClient dynamoDbClient;
  private final S3Presigner s3Presigner;
  private final AwsProperties awsProperties;

  public ProfileServiceImpl(DynamoDbClient dynamoDbClient, S3Presigner s3Presigner, AwsProperties awsProperties) {
    this.dynamoDbClient = dynamoDbClient;
    this.s3Presigner = s3Presigner;
    this.awsProperties = awsProperties;
  }

  @Override
  public ApiResponse getSelfie() {
    logger.info("Getting selfie URL");

    String selfieUrl = awsProperties.getAssetsCloudFront() + Constant.getSelfieKey();

    Map<String, String> data = Map.of("url", selfieUrl);
    return new ApiResponse(STATUS_SUCCESS, 200, "Selfie URL retrieved successfully", data, null);
  }

  @Override
  public ApiResponse getSelfieUploadUrl(String contentType) {
    logger.info("Generating presigned URL for selfie upload with contentType={}", contentType);

    if (!Constant.getAllowedSelfieMimeTypes().contains(contentType)) {
      throw new IllegalArgumentException(
          "Invalid content type. Allowed: " + Constant.getAllowedSelfieMimeTypes());
    }

    return generatePresignedUrl(Constant.getSelfieKey(), contentType, "selfie");
  }

  @Override
  public ApiResponse getResume() {
    logger.info("Getting resume URL");

    String resumeUrl = awsProperties.getAssetsCloudFront() + Constant.getResumeKey();

    Map<String, String> data = Map.of("url", resumeUrl);
    return new ApiResponse(STATUS_SUCCESS, 200, "Resume URL retrieved successfully", data, null);
  }

  @Override
  public ApiResponse getResumeUploadUrl() {
    logger.info("Generating presigned URL for resume upload");
    return generatePresignedUrl(Constant.getResumeKey(), "application/pdf", "resume");
  }

  @Override
  public ApiResponse getSocialLinks() {
    logger.info("Getting social links");

    Map<String, AttributeValue> key = new HashMap<>();
    key.put(ATTR_PROFILE_ID, AttributeValue.builder().s(PROFILE_ID).build());

    GetItemRequest getRequest =
        GetItemRequest.builder()
            .tableName(awsProperties.getProfileTable())
            .key(key)
            .projectionExpression(String.join(", ", ATTR_GITHUB, ATTR_LINKEDIN))
            .build();

    GetItemResponse response = dynamoDbClient.getItem(getRequest);

    Map<String, String> responseData = new HashMap<>();
    Map<String, AttributeValue> item = response.item();

    if (item != null && !item.isEmpty()) {
      if (item.containsKey(ATTR_GITHUB)) {
        responseData.put(ATTR_GITHUB, item.get(ATTR_GITHUB).s());
      }
      if (item.containsKey(ATTR_LINKEDIN)) {
        responseData.put(ATTR_LINKEDIN, item.get(ATTR_LINKEDIN).s());
      }
    }

    return new ApiResponse(STATUS_SUCCESS, 200, "Social links retrieved successfully", responseData, null);
  }

  @Override
  public ApiResponse getSiteMessage() {
    logger.info("Getting site message");

    Map<String, AttributeValue> key = new HashMap<>();
    key.put(ATTR_PROFILE_ID, AttributeValue.builder().s(PROFILE_ID).build());

    GetItemRequest getRequest =
        GetItemRequest.builder()
            .tableName(awsProperties.getProfileTable())
            .key(key)
            .projectionExpression(ATTR_SITE_MESSAGE)
            .build();

    GetItemResponse response = dynamoDbClient.getItem(getRequest);

    Map<String, String> responseData = new HashMap<>();
    Map<String, AttributeValue> item = response.item();

    if (item != null && item.containsKey(ATTR_SITE_MESSAGE)) {
      responseData.put("message", item.get(ATTR_SITE_MESSAGE).s());
    }

    return new ApiResponse(STATUS_SUCCESS, 200, "Site message retrieved successfully", responseData, null);
  }

  @Override
  public ApiResponse saveSocialLinks(SocialLinksRequest request) {
    logger.info("Saving social links to profile");

    if (!request.hasAtLeastOneLink()) {
      throw new IllegalArgumentException("At least one social link must be provided");
    }

    String now = DateTimeUtil.getCurrentTimestamp();

    StringBuilder updateExpression = new StringBuilder("SET ").append(ATTR_UPDATED_AT).append(" = :updatedAt");
    Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
    expressionAttributeValues.put(":updatedAt", AttributeValue.builder().s(now).build());

    if (request.getGithub() != null) {
      updateExpression.append(", ").append(ATTR_GITHUB).append(" = :github");
      expressionAttributeValues.put(":github", AttributeValue.builder().s(request.getGithub()).build());
    }

    if (request.getLinkedin() != null) {
      updateExpression.append(", ").append(ATTR_LINKEDIN).append(" = :linkedin");
      expressionAttributeValues.put(":linkedin", AttributeValue.builder().s(request.getLinkedin()).build());
    }

    Map<String, AttributeValue> key = new HashMap<>();
    key.put(ATTR_PROFILE_ID, AttributeValue.builder().s(PROFILE_ID).build());

    UpdateItemRequest updateRequest =
        UpdateItemRequest.builder()
            .tableName(awsProperties.getProfileTable())
            .key(key)
            .updateExpression(updateExpression.toString())
            .expressionAttributeValues(expressionAttributeValues)
            .build();

    dynamoDbClient.updateItem(updateRequest);
    logger.info("Successfully saved social links");

    Map<String, String> responseData = new HashMap<>();
    if (request.getGithub() != null) {
      responseData.put(ATTR_GITHUB, request.getGithub());
    }
    if (request.getLinkedin() != null) {
      responseData.put(ATTR_LINKEDIN, request.getLinkedin());
    }
    responseData.put(ATTR_UPDATED_AT, now);

    return new ApiResponse(STATUS_SUCCESS, 200, "Social links saved successfully", responseData, null);
  }

  @Override
  public ApiResponse saveSiteMessage(SiteMessageRequest request) {
    logger.info("Saving site message to profile");

    if (request.getMessage() == null || request.getMessage().isBlank()) {
      throw new IllegalArgumentException("Message cannot be blank");
    }

    String now = DateTimeUtil.getCurrentTimestamp();
    String sanitizedMessage = sanitizeMessage(request.getMessage());

    Map<String, AttributeValue> key = new HashMap<>();
    key.put(ATTR_PROFILE_ID, AttributeValue.builder().s(PROFILE_ID).build());

    Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
    expressionAttributeValues.put(":message", AttributeValue.builder().s(sanitizedMessage).build());
    expressionAttributeValues.put(":updatedAt", AttributeValue.builder().s(now).build());

    String updateExpression = "SET " + ATTR_SITE_MESSAGE + " = :message, " + ATTR_UPDATED_AT + " = :updatedAt";

    UpdateItemRequest updateRequest =
        UpdateItemRequest.builder()
            .tableName(awsProperties.getProfileTable())
            .key(key)
            .updateExpression(updateExpression)
            .expressionAttributeValues(expressionAttributeValues)
            .build();

    dynamoDbClient.updateItem(updateRequest);
    logger.info("Successfully saved site message");

    Map<String, String> responseData = Map.of(
        "message", sanitizedMessage,
        ATTR_UPDATED_AT, now);

    return new ApiResponse(STATUS_SUCCESS, 200, "Site message saved successfully", responseData, null);
  }

  private ApiResponse generatePresignedUrl(String s3Key, String contentType, String assetType) {
    PutObjectRequest putObjectRequest =
        PutObjectRequest.builder()
            .bucket(awsProperties.getAssetsBucket())
            .key(s3Key)
            .contentType(contentType)
            .build();

    PresignedPutObjectRequest presignedRequest =
        s3Presigner.presignPutObject(
            presignBuilder ->
                presignBuilder
                    .signatureDuration(PRESIGNED_URL_DURATION)
                    .putObjectRequest(putObjectRequest));

    URL presignedUrl = presignedRequest.url();
    logger.info("Successfully generated presigned URL for {} upload", assetType);

    Map<String, String> data =
        Map.of(
            "url", presignedUrl.toString(),
            "key", s3Key,
            "expiresInMinutes", String.valueOf(PRESIGNED_URL_DURATION.toMinutes()));

    return new ApiResponse(
        STATUS_SUCCESS,
        200,
        String.format("Presigned URL generated for %s upload", assetType),
        data,
        null);
  }

  private String sanitizeMessage(String message) {
    if (message == null) {
      return "";
    }
    String trimmed = message.trim();
    if (trimmed.length() > MAX_MESSAGE_LENGTH) {
      return trimmed.substring(0, MAX_MESSAGE_LENGTH);
    }
    return trimmed;
  }
}
