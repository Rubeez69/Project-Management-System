package com.project_management.final_project.dto.response;

import com.project_management.final_project.entities.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private Integer id;
    private String title;
    private String description;
    private String priority;
    private String status;
    private LocalDate startDate;
    private LocalDate dueDate;
    private Integer projectId;
    private String projectName;
    private UserSummaryResponse assignee;
    private UserSummaryResponse createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static TaskResponse fromEntity(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority().name())
                .status(task.getStatus().name())
                .startDate(task.getStartDate())
                .dueDate(task.getDueDate())
                .projectId(task.getProject() != null ? task.getProject().getId() : null)
                .projectName(task.getProject() != null ? task.getProject().getName() : null)
                .assignee(task.getAssignee() != null ? UserSummaryResponse.fromEntity(task.getAssignee()) : null)
                .createdBy(task.getCreatedBy() != null ? UserSummaryResponse.fromEntity(task.getCreatedBy()) : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
} 