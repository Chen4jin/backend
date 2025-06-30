package com.api.response;

import com.api.common.ApiResponse;

public record PhotoResponse(ApiResponse apiResponse, String lastKey, boolean hasMore) {}
