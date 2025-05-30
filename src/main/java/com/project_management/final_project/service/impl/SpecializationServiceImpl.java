package com.project_management.final_project.service.impl;

import com.project_management.final_project.dto.response.SpecializationResponse;
import com.project_management.final_project.entities.Specialization;
import com.project_management.final_project.repository.SpecializationRepository;
import com.project_management.final_project.service.SpecializationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpecializationServiceImpl implements SpecializationService {

    private static final Logger logger = LoggerFactory.getLogger(SpecializationServiceImpl.class);
    private final SpecializationRepository specializationRepository;

    @Autowired
    public SpecializationServiceImpl(SpecializationRepository specializationRepository) {
        this.specializationRepository = specializationRepository;
    }

    @Override
    public List<SpecializationResponse> getAllSpecializations() {
        try {
            // Get all specializations sorted by name
            List<Specialization> specializations = specializationRepository.findAllByOrderByNameAsc();
            
            // Map to response DTOs
            List<SpecializationResponse> specializationResponses = specializations.stream()
                    .map(SpecializationResponse::fromEntity)
                    .collect(Collectors.toList());
            
            logger.info("Retrieved {} specializations", specializations.size());
            
            return specializationResponses;
        } catch (Exception e) {
            logger.error("Error retrieving specializations: {}", e.getMessage(), e);
            throw e;
        }
    }
} 