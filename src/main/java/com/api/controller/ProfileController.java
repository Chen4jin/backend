package com.api.controller;

import com.api.common.ApiResponse;
import com.api.dto.request.SiteMessageRequest;
import com.api.dto.request.SocialLinksRequest;
import com.api.service.ProfileService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for profile management endpoints.
 * Handles selfie upload, resume upload, social links, and site message operations.
 */
@RestController
@RequestMapping("/v1")
public class ProfileController {

  private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

  private final ProfileService profileService;

  public ProfileController(ProfileService profileService) {
    this.profileService = profileService;
  }

  /**
   * Gets the selfie/profile photo URL.
   *
   * @return CloudFront URL for the selfie
   */
  @GetMapping("/selfie")
  public ResponseEntity<ApiResponse> getSelfie() {
    logger.info("Getting selfie URL");

    ApiResponse response = profileService.getSelfie();
    return ResponseEntity.ok(response);
  }

  /**
   * Generates a presigned URL for uploading a profile photo (selfie).
   *
   * @param contentType the MIME type of the image
   * @return presigned URL and metadata
   */
  @PutMapping("/selfie")
  public ResponseEntity<ApiResponse> getSelfieUploadUrl(
      @RequestParam("contentType") String contentType) {
    logger.info("Generating selfie upload URL for contentType={}", contentType);

    ApiResponse response = profileService.getSelfieUploadUrl(contentType);
    return ResponseEntity.ok(response);
  }

  /**
   * Gets the resume download URL.
   *
   * @return CloudFront URL for the resume
   */
  @GetMapping("/resume")
  public ResponseEntity<ApiResponse> getResume() {
    logger.info("Getting resume URL");

    ApiResponse response = profileService.getResume();
    return ResponseEntity.ok(response);
  }

  /**
   * Generates a presigned URL for uploading a resume.
   *
   * @return presigned URL and metadata
   */
  @PutMapping("/resume")
  public ResponseEntity<ApiResponse> getResumeUploadUrl() {
    logger.info("Generating resume upload URL");

    ApiResponse response = profileService.getResumeUploadUrl();
    return ResponseEntity.ok(response);
  }

  /**
   * Gets the social links.
   *
   * @return social links (GitHub and LinkedIn)
   */
  @GetMapping("/social-links")
  public ResponseEntity<ApiResponse> getSocialLinks() {
    logger.info("Getting social links");

    ApiResponse response = profileService.getSocialLinks();
    return ResponseEntity.ok(response);
  }

  /**
   * Saves or updates social links (GitHub and LinkedIn).
   *
   * @param request the social links
   * @return success confirmation with saved data
   */
  @PostMapping("/social-links")
  public ResponseEntity<ApiResponse> saveSocialLinks(
      @Valid @RequestBody SocialLinksRequest request) {
    logger.info("Saving social links");

    ApiResponse response = profileService.saveSocialLinks(request);
    return ResponseEntity.ok(response);
  }

  /**
   * Gets the site message.
   *
   * @return site message
   */
  @GetMapping("/site-message")
  public ResponseEntity<ApiResponse> getSiteMessage() {
    logger.info("Getting site message");

    ApiResponse response = profileService.getSiteMessage();
    return ResponseEntity.ok(response);
  }

  /**
   * Saves or updates the site message.
   *
   * @param request the site message
   * @return success confirmation with saved message
   */
  @PostMapping("/site-message")
  public ResponseEntity<ApiResponse> saveSiteMessage(
      @Valid @RequestBody SiteMessageRequest request) {
    logger.info("Saving site message");

    ApiResponse response = profileService.saveSiteMessage(request);
    return ResponseEntity.ok(response);
  }

}
