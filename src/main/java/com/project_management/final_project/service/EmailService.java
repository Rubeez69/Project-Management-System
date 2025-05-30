package com.project_management.final_project.service;

public interface EmailService {
    void sendSimpleMessage(String to, String subject, String text);
    void sendHtmlMessage(String to, String subject, String htmlContent);
    
    /**
     * Generates a random OTP and stores it in Redis with a 1-minute expiry time
     * @param email The email address associated with the OTP
     * @return The generated OTP
     */
    String generateOtp(String email);
    
    /**
     * Verifies if the provided OTP matches the one stored in Redis for the given email
     * @param email The email address associated with the OTP
     * @param otp The OTP to verify
     * @return A temporary JWT token if the OTP is valid
     */
    String verifyOtp(String email, String otp);
    
    /**
     * Sends an OTP to the specified email address
     * @param email The email address to send the OTP to
     * @return The generated OTP
     */
    String sendOtp(String email);
    
    /**
     * Generates a temporary JWT token with a 5-minute expiry time
     * @param email The email to store in the token
     * @return The generated JWT token
     */
    String generateToken(String email);
} 