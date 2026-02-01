package com.api.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for saving social links.
 * Validates GitHub and LinkedIn URL formats.
 */
public class SocialLinksRequest {

  @Size(max = 255, message = "GitHub URL must not exceed 255 characters")
  @Pattern(
      regexp = "^(https?://)?(www\\.)?github\\.com/[a-zA-Z0-9_-]+/?$|^$",
      message = "Invalid GitHub URL format")
  private String github;

  @Size(max = 255, message = "LinkedIn URL must not exceed 255 characters")
  @Pattern(
      regexp = "^(https?://)?(www\\.)?linkedin\\.com/in/[a-zA-Z0-9_-]+/?$|^$",
      message = "Invalid LinkedIn URL format")
  private String linkedin;

  public SocialLinksRequest() {}

  public SocialLinksRequest(String github, String linkedin) {
    this.github = github;
    this.linkedin = linkedin;
  }

  public String getGithub() {
    return github;
  }

  public void setGithub(String github) {
    this.github = github;
  }

  public String getLinkedin() {
    return linkedin;
  }

  public void setLinkedin(String linkedin) {
    this.linkedin = linkedin;
  }

  /**
   * Validates that at least one social link is provided.
   *
   * @return true if at least one link is non-null and non-empty
   */
  public boolean hasAtLeastOneLink() {
    return (github != null && !github.isBlank()) || (linkedin != null && !linkedin.isBlank());
  }
}
