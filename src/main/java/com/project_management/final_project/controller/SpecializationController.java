package com.project_management.final_project.controller;

import com.project_management.final_project.dto.response.ApiResponse;
import com.project_management.final_project.dto.response.SpecializationResponse;
import com.project_management.final_project.service.SpecializationService;
import com.project_management.final_project.util.ApiResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/specializations")
@RequiredArgsConstructor
public class SpecializationController {
    
    private final SpecializationService specializationService;
    
    @GetMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER') and hasAuthority('TEAM_CREATE')")
    public ApiResponse<List<SpecializationResponse>> getAllSpecializations() {
        List<SpecializationResponse> specializations = specializationService.getAllSpecializations();
        return ApiResponseUtil.success(specializations);
    }
} 