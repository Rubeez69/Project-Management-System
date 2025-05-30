package com.project_management.final_project.controller;

import com.project_management.final_project.dto.request.OtpRequest;
import com.project_management.final_project.dto.request.VerifyOtpRequest;
import com.project_management.final_project.dto.response.ApiResponse;
import com.project_management.final_project.service.EmailService;
import com.project_management.final_project.util.ApiResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;
    
    @PostMapping("/otp/send")
    public ApiResponse<String> sendOtp(@RequestBody OtpRequest request) {
        String otp = emailService.sendOtp(request.getEmail());
        return ApiResponseUtil.success("OTP sent successfully");
    }
    
    @PostMapping("/otp/verify")
    public ApiResponse<String> verifyOtp(@RequestBody VerifyOtpRequest request) {
        String token = emailService.verifyOtp(request.getEmail(), request.getOtp());
        return ApiResponseUtil.success(token);
    }
    
    @PostMapping("/token/generate")
    public ApiResponse<String> generateToken(@RequestBody OtpRequest request) {
        String token = emailService.generateToken(request.getEmail());
        return ApiResponseUtil.success(token);
    }
} 