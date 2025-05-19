package com.project_management.final_project.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "Passwords do not match"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    INVALID_KEY(HttpStatus.BAD_REQUEST, "Uncategorized error"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token has expired"),
    TOKEN_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "Token signature is invalid"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Token is invalid"),
    TOKEN_MALFORMED(HttpStatus.BAD_REQUEST, "Malformed token"),
    TOKEN_GENERATION_FAILED(HttpStatus.BAD_REQUEST, "Failed to generate tokens"),
    ACCOUNT_INVALID(HttpStatus.BAD_REQUEST, "Email or password invalid!"),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "Unauthenticated");
    private final HttpStatus status;
    private final String message;
}