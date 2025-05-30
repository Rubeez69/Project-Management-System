package com.project_management.final_project.dto.response;

import com.project_management.final_project.entities.Specialization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecializationResponse {
    private Integer id;
    private String name;
    
    public static SpecializationResponse fromEntity(Specialization specialization) {
        if (specialization == null) {
            return null;
        }
        
        return SpecializationResponse.builder()
                .id(specialization.getId())
                .name(specialization.getName())
                .build();
    }
} 