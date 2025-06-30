package com.api.collection;

import com.api.common.ApiResponse;
import com.api.common.S3Connector;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
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

  public ResponseEntity<ApiResponse> putPhoto() {
    String bucketName = "photo-collection-jin";
    String uuid =
        NanoIdUtils.randomNanoId(
            NanoIdUtils.DEFAULT_NUMBER_GENERATOR, NanoIdUtils.DEFAULT_ALPHABET, 4);
    String date = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

    String fileName = new StringBuilder().append(uuid).append(date).append(".jpg").toString();
    String contentType = "image/jpeg";

    PutObjectRequest putObjectRequest =
        PutObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .contentType(contentType)
            .build();

    PresignedPutObjectRequest presignedRequest =
        this.connector
            .getClient()
            .presignPutObject(
                presignRequest ->
                    presignRequest
                        .signatureDuration(Duration.ofMinutes(30))
                        .putObjectRequest(putObjectRequest));

    URL url = presignedRequest.url();

    Map<String, String> map = Map.of("url", url.toString(), "imageID", fileName);

    ApiResponse requestResponse = new ApiResponse(true, "pre-signed url for s3 upload", map);
    return ResponseEntity.ok(requestResponse);
  }
}
