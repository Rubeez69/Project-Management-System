package com.project_management.final_project.util;

import com.project_management.final_project.dto.response.ApiResponse;

public class ApiResponseUtil {
    public static <T> ApiResponse<T> success(T result) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
        response.setResult(result);
        return response;
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
}
