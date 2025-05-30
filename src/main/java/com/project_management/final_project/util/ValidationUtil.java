package com.project_management.final_project.util;

import java.util.regex.Pattern;

/**
 * Utility class for input validation
 */
public class ValidationUtil {

    // Email regex pattern: allows letters, numbers, dots, underscores, percent signs, plus signs, and hyphens in the local part
    // Domain part must be valid with at least one dot
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Password regex pattern: at least 8 characters, at least one letter and one number
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$"
    );

    /**
     * Validates an email address
     * @param email Email address to validate
     * @return true if the email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates a password
     * @param password Password to validate
     * @return true if the password is valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }
} 