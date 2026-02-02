package com.api.service;

import com.api.common.ApiResponse;
import com.api.dto.request.PatchPhotoRequest;
import com.api.dto.request.PutPhotoRequest;
import com.api.dto.response.PhotoResponse;

/**
 * Service interface for photo operations.
 * Handles photo retrieval, upload URL generation, and metadata management.
 */
public interface PhotoService {

  /**
   * Retrieves photos with pagination support.
   *
   * @param lastKey the last evaluated key for pagination (null for first page)
   * @param pageSize number of items per page
   * @return PhotoResponse containing photos and pagination info
   */
  PhotoResponse getPhotos(String lastKey, Integer pageSize);

  /**
   * Generates a presigned URL for uploading a photo to S3.
   *
   * @param contentType the MIME type of the file to upload
   * @return ApiResponse containing the presigned URL and image ID
   */
  ApiResponse generateUploadUrl(String contentType);

  /**
   * Saves photo metadata to the database after upload.
   * Optional EXIF fields in the request are persisted when present.
   *
   * @param request photo metadata (imageID, fileName, sizeBytes) and optional EXIF
   * @return ApiResponse indicating success or failure
   */
  ApiResponse savePhotoMetadata(PutPhotoRequest request);

  /**
   * Updates photo metadata (e.g. display name). Only provided fields are updated.
   *
   * @param imageId the unique identifier for the photo to update
   * @param request fields to update (all optional)
   * @return ApiResponse indicating success or failure
   */
  ApiResponse updatePhotoMetadata(String imageId, PatchPhotoRequest request);

  /**
   * Deletes a photo by setting its isDeleted flag to true (soft delete).
   *
   * @param imageId the unique identifier for the image to delete
   * @return ApiResponse indicating success or failure
   */
  ApiResponse deletePhoto(String imageId);
}
