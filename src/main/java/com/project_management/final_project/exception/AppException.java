package com.project_management.final_project.exception;

import lombok.Getter;

import java.io.Serial;

@Getter
public class AppException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
