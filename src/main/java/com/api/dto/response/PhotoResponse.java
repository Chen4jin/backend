package com.api.dto.response;

import com.api.common.ApiResponse;

/**
 * Response DTO for photo list with pagination support.
 */
public record PhotoResponse(ApiResponse apiResponse, String lastKey, boolean hasMore) {}
