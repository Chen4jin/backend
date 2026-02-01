package com.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Externalized AWS configuration properties.
 * Values are loaded from application.properties or environment variables.
 */
@Component
@ConfigurationProperties(prefix = "app.aws")
public class AwsProperties {

  private String region = "us-east-1";
  private String photosBucket = "photos-jin";
  private String assetsBucket = "generic-jin";
  private String photosCloudFront = "https://d3bjrjf10s3vbi.cloudfront.net/";
  private String assetsCloudFront = "https://d3bjrjf10s3vbi.cloudfront.net/";
  private String profileTable = "tbl_profile";
  private String photoTable = "tbl_photo";

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getPhotosBucket() {
    return photosBucket;
  }

  public void setPhotosBucket(String photosBucket) {
    this.photosBucket = photosBucket;
  }

  public String getAssetsBucket() {
    return assetsBucket;
  }

  public void setAssetsBucket(String assetsBucket) {
    this.assetsBucket = assetsBucket;
  }

  public String getPhotosCloudFront() {
    return photosCloudFront;
  }

  public void setPhotosCloudFront(String photosCloudFront) {
    this.photosCloudFront = photosCloudFront;
  }

  public String getAssetsCloudFront() {
    return assetsCloudFront;
  }

  public void setAssetsCloudFront(String assetsCloudFront) {
    this.assetsCloudFront = assetsCloudFront;
  }

  public String getProfileTable() {
    return profileTable;
  }

  public void setProfileTable(String profileTable) {
    this.profileTable = profileTable;
  }

  public String getPhotoTable() {
    return photoTable;
  }

  public void setPhotoTable(String photoTable) {
    this.photoTable = photoTable;
  }
}
