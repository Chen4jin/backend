package com.api.config;

import com.api.common.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * AWS configuration class providing singleton beans for AWS service clients.
 * Supports both local file-based credentials and default AWS credential chain.
 *
 * <p>Configuration priority:
 * <ol>
 *   <li>application-secrets.properties (aws.access-key-id, aws.secret-access-key)</li>
 *   <li>Environment variables (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)</li>
 *   <li>AWS credentials file (~/.aws/credentials)</li>
 *   <li>IAM role (for EC2/ECS/Lambda)</li>
 * </ol>
 */
@Configuration
public class AwsConfig {

  private static final Logger logger = LoggerFactory.getLogger(AwsConfig.class);

  @Value("${aws.access-key-id:}")
  private String accessKeyId;

  @Value("${aws.secret-access-key:}")
  private String secretAccessKey;

  /**
   * Creates the AWS credentials provider.
   * Uses static credentials from properties file if provided,
   * otherwise falls back to default credential chain.
   *
   * @return configured AwsCredentialsProvider
   */
  @Bean
  public AwsCredentialsProvider awsCredentialsProvider() {
    if (hasLocalCredentials()) {
      logger.info("Using credentials from application-secrets.properties");
      return StaticCredentialsProvider.create(
          AwsBasicCredentials.create(accessKeyId, secretAccessKey));
    }

    logger.info("Using default AWS credentials provider chain");
    return DefaultCredentialsProvider.create();
  }

  /**
   * Creates a singleton DynamoDB client bean.
   *
   * @param credentialsProvider the AWS credentials provider
   * @return configured DynamoDbClient instance
   */
  @Bean
  public DynamoDbClient dynamoDbClient(AwsCredentialsProvider credentialsProvider) {
    logger.info("Initializing DynamoDB client for region: {}", Constant.REGION);
    return DynamoDbClient.builder()
        .region(Constant.REGION)
        .credentialsProvider(credentialsProvider)
        .build();
  }

  /**
   * Creates a singleton S3 presigner bean for generating presigned URLs.
   *
   * @param credentialsProvider the AWS credentials provider
   * @return configured S3Presigner instance
   */
  @Bean
  public S3Presigner s3Presigner(AwsCredentialsProvider credentialsProvider) {
    logger.info("Initializing S3 presigner for region: {}", Constant.REGION);
    return S3Presigner.builder()
        .region(Constant.REGION)
        .credentialsProvider(credentialsProvider)
        .build();
  }

  /**
   * Checks if local credentials are configured in properties file.
   */
  private boolean hasLocalCredentials() {
    return accessKeyId != null && !accessKeyId.isBlank()
        && secretAccessKey != null && !secretAccessKey.isBlank();
  }
}
