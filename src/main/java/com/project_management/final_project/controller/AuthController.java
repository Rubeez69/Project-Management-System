package com.project_management.final_project.controller;

import com.project_management.final_project.dto.request.AuthRequest;
import com.project_management.final_project.dto.request.RefreshTokenRequest;
import com.project_management.final_project.dto.request.ResetPasswordRequest;
import com.project_management.final_project.dto.response.ApiResponse;
import com.project_management.final_project.dto.response.AuthResponse;
import com.project_management.final_project.service.AuthService;
import com.project_management.final_project.util.ApiResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody AuthRequest request) {
        var result = authService.login(request);
        return ApiResponseUtil.success(result);
    }

    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponseUtil.success("Password reset successfully");
    }

    // POST /api/auth/refresh
    @PostMapping("/refresh")
    public ApiResponse<String> refreshToken(@RequestBody RefreshTokenRequest request) {
        var result = authService.refreshAccessToken(request.getRefreshToken());
        return ApiResponseUtil.success(result);
    }
}
