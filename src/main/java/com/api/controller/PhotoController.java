package com.api.controller;

import com.api.common.ApiResponse;
import com.api.dto.request.PutPhotoRequest;
import com.api.dto.response.PhotoResponse;
import com.api.service.PhotoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for photo management endpoints.
 */
@RestController
@RequestMapping("/v1")
public class PhotoController {

  private static final Logger logger = LoggerFactory.getLogger(PhotoController.class);

  private final PhotoService photoService;

  public PhotoController(PhotoService photoService) {
    this.photoService = photoService;
  }

  /**
   * Retrieves photos with pagination support.
   *
   * @param lastKey the last evaluated key for pagination (optional)
   * @param page the page size
   * @return paginated list of photos
   */
  @GetMapping("/images")
  public ResponseEntity<PhotoResponse> getPhotos(
      @RequestParam(value = "lastKey", required = false) String lastKey,
      @RequestParam("page") Integer page) {
    logger.info("Getting photos with lastKey={}, page={}", lastKey, page);

    PhotoResponse response = photoService.getPhotos(lastKey, page);
    return ResponseEntity.ok(response);
  }

  /**
   * Generates a presigned URL for uploading a photo.
   *
   * @param contentType the MIME type of the file
   * @return presigned URL and image ID
   */
  @PutMapping("/images")
  public ResponseEntity<ApiResponse> getUploadUrl(
      @RequestParam("contentType") String contentType) {
    logger.info("Generating upload URL for contentType={}", contentType);

    ApiResponse response = photoService.generateUploadUrl(contentType);
    return ResponseEntity.ok(response);
  }

  /**
   * Saves photo metadata after successful upload.
   *
   * @param request the photo metadata including imageID
   * @return success or error response
   */
  @PostMapping("/images")
  public ResponseEntity<ApiResponse> savePhotoMetadata(
      @Valid @RequestBody PutPhotoRequest request) {
    logger.info("Saving photo metadata for imageId={}", request.getImageID());

    ApiResponse response =
        photoService.savePhotoMetadata(request.getImageID(), request.getFileName(), request.getSizeBytes());
    return ResponseEntity.ok(response);
  }

  /**
   * Deletes a photo by its ID (soft delete).
   *
   * @param imageId the unique identifier of the photo to delete
   * @return success or error response
   */
  @DeleteMapping("/images/{imageId}")
  public ResponseEntity<ApiResponse> deletePhoto(@PathVariable String imageId) {
    logger.info("Deleting photo with imageId={}", imageId);

    ApiResponse response = photoService.deletePhoto(imageId);
    return ResponseEntity.ok(response);
  }
}
