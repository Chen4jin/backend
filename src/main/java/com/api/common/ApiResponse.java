package com.api.common;

public record ApiResponse(String status, Integer code, String message, Object data, String error) {}
