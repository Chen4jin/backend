package com.api.common;

public record ApiResponse(boolean success, String message, Object data) {}
