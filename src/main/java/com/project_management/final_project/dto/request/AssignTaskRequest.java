package com.project_management.final_project.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignTaskRequest {
    
    @NotNull(message = "User ID is required")
    private Integer userId;
} 