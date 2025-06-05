package com.project_management.final_project.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project_management.final_project.entities.Task;
import com.project_management.final_project.exception.AppException;
import com.project_management.final_project.exception.ErrorCode;
import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
public class UpdateTaskStatusRequest {
    
    private final Task.Status status;
    
    @JsonCreator
    public UpdateTaskStatusRequest(@JsonProperty("status") String statusStr) {
        if (statusStr == null || statusStr.isBlank()) {
            throw new AppException(ErrorCode.INVALID_KEY, "Task status cannot be blank");
        }
        
        try {
            this.status = Task.Status.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            String validValues = Arrays.stream(Task.Status.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            throw new AppException(ErrorCode.INVALID_KEY, 
                    "Invalid task status: '" + statusStr + "'. Valid values are: " + validValues);
        }
    }
} 