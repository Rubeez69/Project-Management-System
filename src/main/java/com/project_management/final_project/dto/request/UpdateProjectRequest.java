package com.project_management.final_project.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project_management.final_project.entities.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProjectRequest {
    
    private String name;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    private String description;
    
    private Project.Status status;
} 