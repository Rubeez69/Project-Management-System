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
public class UnassignedTaskFilterRequest {
    private String search;
    private Task.Priority priority;
} 