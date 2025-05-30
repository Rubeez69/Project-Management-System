package com.project_management.final_project.config;

import com.project_management.final_project.exception.AppException;
import com.project_management.final_project.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Utility class for security-related operations
 */
@Component
public class SecurityUtil {

    /**
     * Extracts the user ID from the JWT token in the SecurityContextHolder
     * @return The user ID as an Integer
     * @throws AppException if the user is not authenticated or the ID claim is missing
     */
    public Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        
        Object principal = authentication.getPrincipal();
        
        if (!(principal instanceof Jwt)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        
        Jwt jwt = (Jwt) principal;
        Object idClaim = jwt.getClaim("id");
        
        if (idClaim == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        
        // Handle different possible types of the ID claim
        if (idClaim instanceof Integer) {
            return (Integer) idClaim;
        } else if (idClaim instanceof Number) {
            return ((Number) idClaim).intValue();
        } else if (idClaim instanceof String) {
            try {
                return Integer.parseInt((String) idClaim);
            } catch (NumberFormatException e) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
        }
        
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
    
    /**
     * Checks if the current user is authenticated
     * @return true if authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
    
    /**
     * Gets the email of the current authenticated user
     * @return The user's email as a String
     * @throws AppException if the user is not authenticated or the email claim is missing
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        
        Object principal = authentication.getPrincipal();
        
        if (!(principal instanceof Jwt)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        
        Jwt jwt = (Jwt) principal;
        String email = jwt.getClaimAsString("email");
        
        if (email == null || email.isEmpty()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        
        return email;
    }
} 