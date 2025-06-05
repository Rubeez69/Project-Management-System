package com.project_management.final_project.dto.request;

import com.project_management.final_project.entities.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTaskFilterRequest {
    private String search;
    private Task.Status status;
    private Task.Priority priority;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
} 