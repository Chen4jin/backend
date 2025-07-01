package com.api.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

public class S3Connector {
  private final S3Presigner presigner;
  private static final Logger logger = LoggerFactory.getLogger(S3Connector.class);

  public S3Presigner getClient() {
    return this.presigner;
  }

  public S3Connector() {

    S3Presigner presigner = null;

    try {
      presigner = S3Presigner.builder()
          .region(Constant.getRegion())
          .credentialsProvider(DefaultCredentialsProvider.create())
          .build();
    } catch (SdkClientException e) {
      // Handle client-side exceptions like network errors
      logger.error("Failed to init S3Presigner due to network errors!");
    } catch (AwsServiceException e) {
      // Handle service-side exceptions returned from AWS
      logger.error("Failed to init S3Presigner due to service-side exceptions returned from AWS!");
    } catch (Exception e) {
      // Catch any other unexpected exceptions
      logger.error("unexpected exceptions {}!", e.getMessage(), e);
    }
    this.presigner = presigner;
  }
}
