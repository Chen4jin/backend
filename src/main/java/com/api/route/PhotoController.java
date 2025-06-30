package com.api.route;

import com.api.collection.PhotoDB;
import com.api.collection.PhotoS3;
import com.api.common.ApiResponse;
import com.api.records.PostPhoto;
import com.api.response.PhotoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PhotoController {
  @CrossOrigin(origins = "http://localhost:3000")
  @GetMapping("/photo")
  public ResponseEntity<PhotoResponse> getPhoto(
      @RequestParam(value = "lastKey", required = false) String lastKey,
      @RequestParam("page") Integer page) {
    PhotoDB photo = new PhotoDB();
    return photo.getPhoto(lastKey, page);
  }

  @CrossOrigin(origins = "http://localhost:3000")
  @PutMapping("/photo")
  public ResponseEntity<ApiResponse> putPhoto() {
    PhotoS3 photo = new PhotoS3();
    return photo.putPhoto();
  }

  @CrossOrigin(origins = "http://localhost:3000")
  @PostMapping("/photo")
  public ResponseEntity<ApiResponse> postPhoto(@RequestBody PostPhoto request) {
    PhotoDB photo = new PhotoDB();
    return photo.postPhoto(request.getImageID(), request.getfileName(), request.getSizeBytes());
  }
}
