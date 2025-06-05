package com.project_management.final_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskHistoryResponse {
    private Integer id;
    private String message;
    private LocalDateTime changedAt;
} 