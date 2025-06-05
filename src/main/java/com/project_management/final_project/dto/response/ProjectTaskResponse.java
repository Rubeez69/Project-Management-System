package com.project_management.final_project.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project_management.final_project.entities.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTaskResponse {
    private Integer id;
    private String title;
    private String assignedTo;
    private Task.Status status;
    
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dueDate;
    
    private Task.Priority priority;
    
    /**
     * Convert a Task entity to a ProjectTaskResponse DTO
     * @param task The task entity to convert
     * @return The ProjectTaskResponse DTO
     */
    public static ProjectTaskResponse fromEntity(Task task) {
        return ProjectTaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .assignedTo(task.getAssignee() != null ? task.getAssignee().getName() : null)
                .status(task.getStatus())
                .startDate(task.getStartDate())
                .dueDate(task.getDueDate())
                .priority(task.getPriority())
                .build();
    }
} 