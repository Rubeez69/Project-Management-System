package com.project_management.final_project.service;

import com.project_management.final_project.dto.response.SpecializationResponse;

import java.util.List;

public interface SpecializationService {
    
    /**
     * Get all specializations
     * @return List of specializations
     */
    List<SpecializationResponse> getAllSpecializations();
} 