package com.api.common;

import java.util.Map;
import java.util.Set;
import software.amazon.awssdk.regions.Region;

/**
 * Application-wide constants for AWS configuration, MIME types, and resource identifiers.
 */
public final class Constant {

  // Private constructor to prevent instantiation
  private Constant() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  // AWS Region configuration
  public static final Region REGION = Region.US_EAST_1;

  // S3 Buckets
  public static final String PHOTOS_BUCKET = "photos-jin";
  public static final String ASSETS_BUCKET = "generic-jin";

  // S3 key prefixes for static assets
  public static final String STATIC_PREFIX = "static/";
  public static final String SELFIE_KEY = STATIC_PREFIX + "selfie";
  public static final String RESUME_KEY = STATIC_PREFIX + "resume";

  // Image MIME type to extension mapping
  public static final Map<String, String> MIME_TO_EXT =
      Map.of(
          "image/jpeg", ".jpg",
          "image/png", ".png",
          "image/gif", ".gif",
          "image/webp", ".webp");

  // Resume/document MIME type to extension mapping
  public static final Map<String, String> RESUME_MIME_TO_EXT =
      Map.of(
          "application/pdf", ".pdf",
          "application/msword", ".doc",
          "application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx");

  // Allowed MIME types for profile photos (selfie)
  public static final Set<String> ALLOWED_SELFIE_MIME_TYPES =
      Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

  // Allowed MIME types for resume uploads
  public static final Set<String> ALLOWED_RESUME_MIME_TYPES =
      Set.of(
          "application/pdf",
          "application/msword",
          "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

  // DynamoDB table for profile data
  public static final String PROFILE_TABLE_NAME = "tbl_profile";

  public static Region getRegion() {
    return REGION;
  }

  public static String getPhotosBucket() {
    return PHOTOS_BUCKET;
  }

  public static String getAssetsBucket() {
    return ASSETS_BUCKET;
  }

  public static String getSelfieKey() {
    return SELFIE_KEY;
  }

  public static String getResumeKey() {
    return RESUME_KEY;
  }

  public static Map<String, String> getMimeToExt() {
    return MIME_TO_EXT;
  }

  public static Map<String, String> getResumeMimeToExt() {
    return RESUME_MIME_TO_EXT;
  }

  public static Set<String> getAllowedSelfieMimeTypes() {
    return ALLOWED_SELFIE_MIME_TYPES;
  }

  public static Set<String> getAllowedResumeMimeTypes() {
    return ALLOWED_RESUME_MIME_TYPES;
  }

  public static String getProfileTableName() {
    return PROFILE_TABLE_NAME;
  }
}
