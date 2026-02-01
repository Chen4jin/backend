package com.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for saving photo metadata after upload.
 */
public class PutPhotoRequest {

  @NotBlank(message = "Image ID is required")
  private String imageID;

  @NotBlank(message = "File name is required")
  private String fileName;

  @NotBlank(message = "File size is required")
  @Pattern(regexp = "^\\d+$", message = "Size must be a valid number")
  private String sizeBytes;

  public PutPhotoRequest() {}

  public PutPhotoRequest(String imageID, String fileName, String sizeBytes) {
    this.imageID = imageID;
    this.fileName = fileName;
    this.sizeBytes = sizeBytes;
  }

  public String getImageID() {
    return imageID;
  }

  public void setImageID(String imageID) {
    this.imageID = imageID;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getSizeBytes() {
    return sizeBytes;
  }

  public void setSizeBytes(String sizeBytes) {
    this.sizeBytes = sizeBytes;
  }
}
