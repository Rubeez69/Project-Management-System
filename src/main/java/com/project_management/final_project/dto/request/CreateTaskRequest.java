package com.project_management.final_project.dto.request;

import com.project_management.final_project.entities.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {
    
    @NotBlank(message = "Please fill in all required fields.")
    @Size(max = 255, message = "Task name cannot exceed 255 characters.")
    private String title;
    
    private String description;
    
    private Task.Priority priority;
    
    @NotNull(message = "Please fill in all required fields.")
    private LocalDate startDate;
    
    private LocalDate dueDate;
    
    private Integer assigneeId;
} 