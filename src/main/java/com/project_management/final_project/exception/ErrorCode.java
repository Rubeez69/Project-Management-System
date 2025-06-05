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
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request"),
    DUPLICATE_ENTITY(HttpStatus.CONFLICT, "Entity already exists"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token has expired"),
    TOKEN_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "Token signature is invalid"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Token is invalid"),
    TOKEN_MALFORMED(HttpStatus.BAD_REQUEST, "Malformed token"),
    TOKEN_GENERATION_FAILED(HttpStatus.BAD_REQUEST, "Failed to generate tokens"),
    ACCOUNT_INVALID(HttpStatus.BAD_REQUEST, "Email or password invalid!"),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "Unauthenticated"),
    OTP_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate OTP"),
    OTP_EXPIRED(HttpStatus.BAD_REQUEST, "OTP has expired"),
    OTP_INVALID(HttpStatus.BAD_REQUEST, "Invalid OTP"),
    EMAIL_SENDING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email"),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "Invalid email format. Please use a valid email address."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "Invalid password format. Password must be at least 8 characters long and contain at least one letter and one number."),
    PASSWORD_RESET_FAILED(HttpStatus.BAD_REQUEST, "Password reset failed. The token may be invalid or expired."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    UNAUTHORIZED(HttpStatus.FORBIDDEN, "Not authorized to perform this action");
    
    private final HttpStatus status;
    private final String message;
}