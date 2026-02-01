package com.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for saving site message.
 * Validates message content constraints.
 */
public class SiteMessageRequest {

  @NotBlank(message = "Message cannot be blank")
  @Size(min = 1, max = 1000, message = "Message must be between 1 and 1000 characters")
  private String message;

  public SiteMessageRequest() {}

  public SiteMessageRequest(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
