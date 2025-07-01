package com.api.collection;

import com.api.common.ApiResponse;
import com.api.common.S3Connector;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.api.common.Constant;
import java.net.URL;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

public class PhotoS3 {
  private final S3Connector connector;

  public S3Connector getConnector() {
    return this.connector;
  }

  public PhotoS3() {
    connector = new S3Connector();
  }

  public ResponseEntity<ApiResponse> uploadURL(String contentType) {
    // Define the S3 bucket name where the image will be uploaded
    String bucketName = "photo-collection-jin";

    // Generate a short unique ID (4 characters) using NanoId for filename
    // uniqueness
    String uuid = NanoIdUtils.randomNanoId(
        NanoIdUtils.DEFAULT_NUMBER_GENERATOR, NanoIdUtils.DEFAULT_ALPHABET, 4);

    // Get the current date formatted as yyyyMMdd (e.g., 20250630)
    String date = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

    // Get the ext from content type for the image
    String ext = Constant.getMimeToExt().getOrDefault(contentType, "");

    // Construct the file name by combining uuid, date, and file extension
    // Example: "a1b220250630.jpg"
    String fileName = new StringBuilder().append(uuid).append(date).append(ext).toString();

    // Build a PutObjectRequest with bucket name, file name (key), and content type
    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(fileName)
        .contentType(contentType)
        .build();

    // Create a Duration object representing a time span of 2 minutes.
    Duration duration = Duration.ofMinutes(2);
    URL url;
    try {
      // Create a presigned PUT request for uploading the object to S3
      PresignedPutObjectRequest presignedRequest = this.connector
          .getClient()
          .presignPutObject(
              presignRequest -> presignRequest
                  .signatureDuration(duration)
                  .putObjectRequest(putObjectRequest));
      url = presignedRequest.url();

    } catch (NullPointerException e) {
      // Handle the exception, e.g., log or recover gracefully
      ApiResponse response = new ApiResponse("error", 500, "Failed to initialized S3Connector", "", e.getMessage());
      return ResponseEntity.internalServerError().body(response);
    }
    // Extract the presigned URL which clients can use to upload the file directly
    // to S3

    Map<String, String> data = Map.of("url", url.toString(), "imageID", fileName);

    ApiResponse requestResponse = new ApiResponse("success", 200, "pre-signed url for s3 upload", data, null);
    return ResponseEntity.ok(requestResponse);
  }
}
