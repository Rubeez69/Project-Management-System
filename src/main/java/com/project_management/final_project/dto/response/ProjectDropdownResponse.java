package com.project_management.final_project.dto.response;

import com.project_management.final_project.entities.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDropdownResponse {
    private Integer id;
    private String name;
    
    public static ProjectDropdownResponse fromEntity(Project project) {
        return ProjectDropdownResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .build();
    }
} 