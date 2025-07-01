package com.api.routes;

import com.api.collection.PhotoDB;
import com.api.collection.PhotoS3;
import com.api.common.ApiResponse;
import com.api.records.PutPhotoRequest;
import com.api.records.UploadURLRequest;
import com.api.response.PhotoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class PhotoController {
  @GetMapping("/images")
  public ResponseEntity<PhotoResponse> getPhoto(
      @RequestParam(value = "lastKey", required = false) String lastKey,
      @RequestParam("page") Integer page) {
    PhotoDB photo = new PhotoDB();
    return photo.getPhoto(lastKey, page);
  }

  @PostMapping("/images/upload-url")
  public ResponseEntity<ApiResponse> uploadURL(@RequestBody UploadURLRequest request) {
    PhotoS3 photo = new PhotoS3();
    return photo.uploadURL(request.getContentType());
  }

  @PutMapping("/images/{imagesId}")
  public ResponseEntity<ApiResponse> postPhoto(
      @PathVariable String imagesId, @RequestBody PutPhotoRequest request) {
    PhotoDB photo = new PhotoDB();
    return photo.postPhoto(imagesId, request.getfileName(), request.getSizeBytes());
  }
}
