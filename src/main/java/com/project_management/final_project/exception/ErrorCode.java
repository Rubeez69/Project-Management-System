package com.project_management.final_project.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    PASSWORD_NOTMATCH(HttpStatus.BAD_REQUEST, "Passwords do not match"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    INVALID_KEY(HttpStatus.BAD_REQUEST, "Uncategorized error");
    private final HttpStatus status;
    private final String message;
}