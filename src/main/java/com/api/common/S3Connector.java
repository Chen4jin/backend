package com.api.common;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

public class S3Connector {
  private final S3Presigner presigner;

  public S3Presigner getClient() {
    return this.presigner;
  }

  public S3Connector() {
    Region region = Region.US_EAST_1;
    S3Presigner presigner = null;

    presigner =
        S3Presigner.builder()
            .region(region)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();

    this.presigner = presigner;
  }
}
