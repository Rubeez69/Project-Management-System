package com.project_management.final_project.dto.response;

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
public class UnassignedTaskResponse {
    private Integer id;
    private String title;
    private String description;
    private String priority;
    private LocalDate dueDate;
    
    public static UnassignedTaskResponse fromEntity(Task task) {
        if (task == null) {
            return null;
        }
        
        return UnassignedTaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority() != null ? task.getPriority().name() : null)
                .dueDate(task.getDueDate())
                .build();
    }
} 