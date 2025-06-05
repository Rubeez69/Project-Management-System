package com.project_management.final_project.validator;

import com.project_management.final_project.dto.request.CreateTaskRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class DateValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return CreateTaskRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CreateTaskRequest request = (CreateTaskRequest) target;
        
        // Check if both dates are provided
        if (request.getStartDate() != null && request.getDueDate() != null) {
            // Check if start date is after due date
            if (request.getStartDate().isAfter(request.getDueDate())) {
                errors.rejectValue("startDate", "startDate.afterDueDate", 
                        "Start date cannot be after end date.");
            }
        }
    }
} 