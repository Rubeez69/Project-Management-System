package com.project_management.final_project.exception;

import com.project_management.final_project.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = RuntimeException.class)

    public ResponseEntity<ApiResponse<?>> handlingRuntimeException(RuntimeException exception) {
        ApiResponse<?> apiResponse = new ApiResponse<>();
        String error = exception.getMessage();
        ErrorCode errorCode;
        try {
            errorCode = ErrorCode.valueOf(error);
        } catch (IllegalArgumentException e) {
            errorCode = ErrorCode.INTERNAL_ERROR;
        }
        apiResponse.setCode(errorCode.getStatus().value());
        apiResponse.setMessage(errorCode.getMessage());
        return new ResponseEntity<>(apiResponse, errorCode.getStatus());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)

    public ResponseEntity<ApiResponse<?>> handlingValidation(MethodArgumentNotValidException exception) {
        String message = exception.getFieldError().getDefaultMessage();
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        ApiResponse<?> apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorCode.getStatus().value());
        apiResponse.setMessage(message);
        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<?>> handleAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse<?> apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorCode.getStatus().value());
        apiResponse.setMessage(errorCode.getMessage());
        return new ResponseEntity<>(apiResponse, errorCode.getStatus());
    }
}
