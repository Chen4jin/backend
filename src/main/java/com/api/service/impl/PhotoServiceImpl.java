package com.api.service.impl;

import com.api.common.ApiResponse;
import com.api.common.Constant;
import com.api.config.AwsProperties;
import com.api.dto.request.PatchPhotoRequest;
import com.api.dto.request.PutPhotoRequest;
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
  private static final String ATTR_UPDATED_AT = "updatedAt";
  private static final String ATTR_TITLE = "title";
  private static final String ATTR_DESCRIPTION = "description";
  private static final String ATTR_CAMERA = "camera";
  private static final String ATTR_LENS = "lens";
  private static final String ATTR_APERTURE = "aperture";
  private static final String ATTR_SHUTTER = "shutter";
  private static final String ATTR_ISO = "iso";
  private static final String ATTR_FOCAL_LENGTH = "focalLength";
  private static final String ATTR_LOCATION = "location";
  private static final String ATTR_DATE_TAKEN = "dateTaken";

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

  // Expression attribute name placeholders (avoid DynamoDB reserved words e.g. description, date)
  private static final String P_IMAGE_ID = "#pid";
  private static final String P_FILE_NAME = "#pfn";
  private static final String P_CLOUD_FRONT = "#pcf";
  private static final String P_IS_DELETED = "#pdel";
  private static final String P_TITLE = "#pt";
  private static final String P_DESCRIPTION = "#pdesc";
  private static final String P_CAMERA = "#pcam";
  private static final String P_LENS = "#plens";
  private static final String P_APERTURE = "#papt";
  private static final String P_SHUTTER = "#pshut";
  private static final String P_ISO = "#piso";
  private static final String P_FOCAL_LENGTH = "#pfl";
  private static final String P_LOCATION = "#ploc";
  private static final String P_DATE_TAKEN = "#pdt";
  private static final String P_UPDATED_AT = "#pupd";

  @Override
  public PhotoResponse getPhotos(String lastKey, Integer pageSize) {
    // Validate and sanitize pageSize
    int validPageSize = (pageSize == null || pageSize <= 0) ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
    logger.debug("Fetching photos with lastKey={}, pageSize={}", lastKey, validPageSize);

    String projectionExpression =
        String.join(
            ", ",
            P_IMAGE_ID,
            P_FILE_NAME,
            P_CLOUD_FRONT,
            P_TITLE,
            P_DESCRIPTION,
            P_CAMERA,
            P_LENS,
            P_APERTURE,
            P_SHUTTER,
            P_ISO,
            P_FOCAL_LENGTH,
            P_LOCATION,
            P_DATE_TAKEN,
            P_UPDATED_AT);
    Map<String, String> expressionAttributeNames = new HashMap<>();
    expressionAttributeNames.put(P_IMAGE_ID, ATTR_IMAGE_ID);
    expressionAttributeNames.put(P_FILE_NAME, ATTR_FILE_NAME);
    expressionAttributeNames.put(P_CLOUD_FRONT, ATTR_CLOUD_FRONT);
    expressionAttributeNames.put(P_IS_DELETED, ATTR_IS_DELETED);
    expressionAttributeNames.put(P_TITLE, ATTR_TITLE);
    expressionAttributeNames.put(P_DESCRIPTION, ATTR_DESCRIPTION);
    expressionAttributeNames.put(P_CAMERA, ATTR_CAMERA);
    expressionAttributeNames.put(P_LENS, ATTR_LENS);
    expressionAttributeNames.put(P_APERTURE, ATTR_APERTURE);
    expressionAttributeNames.put(P_SHUTTER, ATTR_SHUTTER);
    expressionAttributeNames.put(P_ISO, ATTR_ISO);
    expressionAttributeNames.put(P_FOCAL_LENGTH, ATTR_FOCAL_LENGTH);
    expressionAttributeNames.put(P_LOCATION, ATTR_LOCATION);
    expressionAttributeNames.put(P_DATE_TAKEN, ATTR_DATE_TAKEN);
    expressionAttributeNames.put(P_UPDATED_AT, ATTR_UPDATED_AT);

    String filterExpression = P_IS_DELETED + " = :val";
    Map<String, AttributeValue> expressionAttributeValues =
        Map.of(":val", AttributeValue.builder().bool(false).build());

    ScanRequest.Builder scanBuilder =
        ScanRequest.builder()
            .tableName(awsProperties.getPhotoTable())
            .projectionExpression(projectionExpression)
            .expressionAttributeNames(expressionAttributeNames)
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
  public ApiResponse savePhotoMetadata(PutPhotoRequest request) {
    String imageId = request.getImageID();
    String fileName = request.getFileName();
    String sizeBytes = request.getSizeBytes();
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

    // Optional EXIF from client-side extraction on upload
    if (hasValue(request.getTitle())) {
      item.put(ATTR_TITLE, AttributeValue.builder().s(request.getTitle().trim()).build());
    }
    if (hasValue(request.getDescription())) {
      item.put(ATTR_DESCRIPTION, AttributeValue.builder().s(request.getDescription().trim()).build());
    }
    if (hasValue(request.getCamera())) {
      item.put(ATTR_CAMERA, AttributeValue.builder().s(request.getCamera().trim()).build());
    }
    if (hasValue(request.getLens())) {
      item.put(ATTR_LENS, AttributeValue.builder().s(request.getLens().trim()).build());
    }
    if (hasValue(request.getAperture())) {
      item.put(ATTR_APERTURE, AttributeValue.builder().s(request.getAperture().trim()).build());
    }
    if (hasValue(request.getShutter())) {
      item.put(ATTR_SHUTTER, AttributeValue.builder().s(request.getShutter().trim()).build());
    }
    if (hasValue(request.getIso())) {
      item.put(ATTR_ISO, AttributeValue.builder().s(request.getIso().trim()).build());
    }
    if (hasValue(request.getFocalLength())) {
      item.put(ATTR_FOCAL_LENGTH, AttributeValue.builder().s(request.getFocalLength().trim()).build());
    }
    if (hasValue(request.getLocation())) {
      item.put(ATTR_LOCATION, AttributeValue.builder().s(request.getLocation().trim()).build());
    }
    if (hasValue(request.getDateTaken())) {
      item.put(ATTR_DATE_TAKEN, AttributeValue.builder().s(request.getDateTaken().trim()).build());
    }

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
  public ApiResponse updatePhotoMetadata(String imageId, PatchPhotoRequest request) {
    if (imageId == null || imageId.isBlank()) {
      throw new IllegalArgumentException("Image ID is required");
    }
    if (request == null) {
      throw new IllegalArgumentException("Request body is required");
    }

    Map<String, String> updates = new HashMap<>();
    if (hasValue(request.getFileName())) {
      updates.put(ATTR_FILE_NAME, request.getFileName().trim());
    }
    if (hasValue(request.getTitle())) {
      updates.put(ATTR_TITLE, request.getTitle().trim());
    }
    if (hasValue(request.getDescription())) {
      updates.put(ATTR_DESCRIPTION, request.getDescription().trim());
    }
    if (hasValue(request.getCamera())) {
      updates.put(ATTR_CAMERA, request.getCamera().trim());
    }
    if (hasValue(request.getLens())) {
      updates.put(ATTR_LENS, request.getLens().trim());
    }
    if (hasValue(request.getAperture())) {
      updates.put(ATTR_APERTURE, request.getAperture().trim());
    }
    if (hasValue(request.getShutter())) {
      updates.put(ATTR_SHUTTER, request.getShutter().trim());
    }
    if (hasValue(request.getIso())) {
      updates.put(ATTR_ISO, request.getIso().trim());
    }
    if (hasValue(request.getFocalLength())) {
      updates.put(ATTR_FOCAL_LENGTH, request.getFocalLength().trim());
    }
    if (hasValue(request.getLocation())) {
      updates.put(ATTR_LOCATION, request.getLocation().trim());
    }
    if (hasValue(request.getDateTaken())) {
      updates.put(ATTR_DATE_TAKEN, request.getDateTaken().trim());
    }

    if (updates.isEmpty()) {
      throw new IllegalArgumentException("At least one metadata field must be provided");
    }

    String now = DateTimeUtil.getCurrentTimestamp();
    updates.put(ATTR_UPDATED_AT, now);

    Map<String, AttributeValue> key = new HashMap<>();
    key.put(ATTR_IMAGE_ID, AttributeValue.builder().s(imageId).build());

    Map<String, String> expressionAttributeNames = new HashMap<>();
    Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
    List<String> setClauses = new ArrayList<>();

    int i = 0;
    for (Map.Entry<String, String> e : updates.entrySet()) {
      String namePlaceholder = "#a" + i;
      String valuePlaceholder = ":v" + i;
      expressionAttributeNames.put(namePlaceholder, e.getKey());
      expressionAttributeValues.put(valuePlaceholder, AttributeValue.builder().s(e.getValue()).build());
      setClauses.add(namePlaceholder + " = " + valuePlaceholder);
      i++;
    }

    expressionAttributeValues.put(":notDeleted", AttributeValue.builder().bool(false).build());

    String updateExpression =
        "SET " + String.join(", ", setClauses);
    String conditionExpression =
        "attribute_exists(" + ATTR_IMAGE_ID + ") AND " + ATTR_IS_DELETED + " = :notDeleted";

    UpdateItemRequest updateRequest =
        UpdateItemRequest.builder()
            .tableName(awsProperties.getPhotoTable())
            .key(key)
            .updateExpression(updateExpression)
            .expressionAttributeNames(expressionAttributeNames)
            .expressionAttributeValues(expressionAttributeValues)
            .conditionExpression(conditionExpression)
            .build();

    try {
      dynamoDbClient.updateItem(updateRequest);
      logger.info("Updated photo metadata for imageId={}", imageId);
      return new ApiResponse(
          "success", 200, "Photo metadata updated successfully", null, null);
    } catch (ConditionalCheckFailedException e) {
      logger.warn("Photo not found for update: imageId={}", imageId);
      return new ApiResponse("error", 404, "Photo not found", null, e.getMessage());
    }
  }

  private static boolean hasValue(String s) {
    return s != null && !s.isBlank();
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
