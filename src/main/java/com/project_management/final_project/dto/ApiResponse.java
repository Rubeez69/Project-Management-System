package com.project_management.final_project.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;           // HTTP status code
    private String message;     // Description of the result or error
    private T result;           // Generic response payload (optional)
}
