package com.project_management.final_project.dto.request;

import com.project_management.final_project.entities.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectFilterRequest {
    private String name;
    private Project.Status status;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
} 