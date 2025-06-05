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
public class UpcomingDueTaskResponse {
    private Integer id;
    private String name; // Task title
    private String status;
    private LocalDate dueDate;
    
    public static UpcomingDueTaskResponse fromEntity(Task task) {
        if (task == null) {
            return null;
        }
        
        return UpcomingDueTaskResponse.builder()
                .id(task.getId())
                .name(task.getTitle())
                .status(task.getStatus() != null ? task.getStatus().name() : null)
                .dueDate(task.getDueDate())
                .build();
    }
} 