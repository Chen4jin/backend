package com.api.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating photo metadata (edit metadata from UI).
 * All fields are optional; only provided fields are updated.
 */
public class PatchPhotoRequest {

  @Size(max = 512, message = "File name must not exceed 512 characters")
  private String fileName;

  @Size(max = 512, message = "Title must not exceed 512 characters")
  private String title;

  @Size(max = 2048, message = "Description must not exceed 2048 characters")
  private String description;

  @Size(max = 256, message = "Camera must not exceed 256 characters")
  private String camera;

  @Size(max = 256, message = "Lens must not exceed 256 characters")
  private String lens;

  @Size(max = 64, message = "Aperture must not exceed 64 characters")
  private String aperture;

  @Size(max = 64, message = "Shutter must not exceed 64 characters")
  private String shutter;

  @Size(max = 32, message = "ISO must not exceed 32 characters")
  private String iso;

  @Size(max = 64, message = "Focal length must not exceed 64 characters")
  private String focalLength;

  @Size(max = 512, message = "Location must not exceed 512 characters")
  private String location;

  /** Date taken (e.g. ISO 8601 string from payload). */
  @Size(max = 64, message = "Date taken must not exceed 64 characters")
  private String dateTaken;

  /** Default constructor for JSON deserialization. */
  public PatchPhotoRequest() {}

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getCamera() {
    return camera;
  }

  public void setCamera(String camera) {
    this.camera = camera;
  }

  public String getLens() {
    return lens;
  }

  public void setLens(String lens) {
    this.lens = lens;
  }

  public String getAperture() {
    return aperture;
  }

  public void setAperture(String aperture) {
    this.aperture = aperture;
  }

  public String getShutter() {
    return shutter;
  }

  public void setShutter(String shutter) {
    this.shutter = shutter;
  }

  public String getIso() {
    return iso;
  }

  public void setIso(String iso) {
    this.iso = iso;
  }

  public String getFocalLength() {
    return focalLength;
  }

  public void setFocalLength(String focalLength) {
    this.focalLength = focalLength;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getDateTaken() {
    return dateTaken;
  }

  public void setDateTaken(String dateTaken) {
    this.dateTaken = dateTaken;
  }
}
