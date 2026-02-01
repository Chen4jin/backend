package com.api.service;

import com.api.common.ApiResponse;
import com.api.dto.request.SiteMessageRequest;
import com.api.dto.request.SocialLinksRequest;

/**
 * Service interface for profile operations.
 * Handles profile photo, resume uploads, social links, and site message.
 */
public interface ProfileService {

  /**
   * Gets the selfie/profile photo URL.
   *
   * @return ApiResponse containing the CloudFront URL
   */
  ApiResponse getSelfie();

  /**
   * Generates a presigned URL for uploading a profile photo (selfie).
   *
   * @param contentType the MIME type of the image
   * @return ApiResponse containing the presigned URL
   */
  ApiResponse getSelfieUploadUrl(String contentType);

  /**
   * Gets the resume download URL.
   *
   * @return ApiResponse containing the CloudFront URL
   */
  ApiResponse getResume();

  /**
   * Generates a presigned URL for uploading a resume.
   *
   * @return ApiResponse containing the presigned URL
   */
  ApiResponse getResumeUploadUrl();

  /**
   * Gets the social links from the profile.
   *
   * @return ApiResponse containing social links
   */
  ApiResponse getSocialLinks();

  /**
   * Saves or updates social links (GitHub and LinkedIn) in the profile.
   *
   * @param request the social links request
   * @return ApiResponse indicating success or failure
   */
  ApiResponse saveSocialLinks(SocialLinksRequest request);

  /**
   * Gets the site message from the profile.
   *
   * @return ApiResponse containing the site message
   */
  ApiResponse getSiteMessage();

  /**
   * Saves or updates the site message in the profile.
   *
   * @param request the site message request
   * @return ApiResponse indicating success or failure
   */
  ApiResponse saveSiteMessage(SiteMessageRequest request);
}
