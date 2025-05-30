package com.project_management.final_project.service.impl;

import com.project_management.final_project.config.JwtKeyProvider;
import com.project_management.final_project.exception.AppException;
import com.project_management.final_project.exception.ErrorCode;
import com.project_management.final_project.repository.UserRepository;
import com.project_management.final_project.service.EmailService;
import com.project_management.final_project.service.RedisService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final String OTP_PREFIX = "otp:";
    private static final int OTP_LENGTH = 6;
    private static final long OTP_EXPIRY_TIME = 1; // 1 minute
    private static final long TOKEN_EXPIRY_TIME = 5 * 60 * 1000; // 5 minutes in milliseconds
    
    private final JavaMailSender emailSender;
    private final RedisService redisService;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom;
    private final JwtKeyProvider jwtKeyProvider;

    @Autowired
    public EmailServiceImpl(JavaMailSender emailSender, RedisService redisService, UserRepository userRepository, JwtKeyProvider jwtKeyProvider) {
        this.emailSender = emailSender;
        this.redisService = redisService;
        this.userRepository = userRepository;
        this.secureRandom = new SecureRandom();
        this.jwtKeyProvider = jwtKeyProvider;
    }

    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
            logger.info("Simple email sent to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send simple email to {}: {}", to, e.getMessage());
            throw new AppException(ErrorCode.EMAIL_SENDING_FAILED);
        }
    }

    @Override
    public void sendHtmlMessage(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            emailSender.send(message);
            logger.info("HTML email sent to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send HTML email to {}: {}", to, e.getMessage());
            throw new AppException(ErrorCode.EMAIL_SENDING_FAILED);
        }
    }
    
    @Override
    public String generateOtp(String email) {
        try {
            // Check if the email exists in the database
            if (!userRepository.existsByEmail(email)) {
                logger.warn("Email not found in the database: {}", email);
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }
            
            String otp = generateRandomOtp();
            String key = OTP_PREFIX + email;
            
            // Store OTP in Redis with 1-minute expiry
            redisService.set(key, otp, OTP_EXPIRY_TIME, TimeUnit.MINUTES);
            
            logger.info("OTP generated for email: {}", email);
            return otp;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to generate OTP for email {}: {}", email, e.getMessage());
            throw new AppException(ErrorCode.OTP_GENERATION_FAILED);
        }
    }
    
    @Override
    public String verifyOtp(String email, String otp) {
        try {
            // Check if the email exists in the database
            if (!userRepository.existsByEmail(email)) {
                logger.warn("Email not found in the database: {}", email);
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }
            
            String key = OTP_PREFIX + email;
            
            // Check if OTP exists in Redis
            if (!redisService.hasKey(key)) {
                logger.warn("OTP not found or expired for email: {}", email);
                throw new AppException(ErrorCode.OTP_EXPIRED);
            }
            
            // Get stored OTP
            String storedOtp = (String) redisService.get(key);
            
            // Verify OTP
            if (storedOtp != null && storedOtp.equals(otp)) {
                // Delete OTP after successful verification
                redisService.delete(key);
                logger.info("OTP verified successfully for email: {}", email);
                
                // Generate and return a temporary token
                return generateToken(email);
            } else {
                logger.warn("Invalid OTP provided for email: {}", email);
                throw new AppException(ErrorCode.OTP_INVALID);
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error verifying OTP for email {}: {}", email, e.getMessage());
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }
    }
    
    @Override
    public String generateToken(String email) {
        try {
            // Use the JwtKeyProvider to get the key
            SecretKey key = jwtKeyProvider.getKey();
            
            String token = Jwts.builder()
                    .claims()
                    .add("email", email)
                    .add("type", "OTP_VERIFICATION")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRY_TIME))
                    .and()
                    .signWith(key, Jwts.SIG.HS512)
                    .compact();
            
            logger.info("Temporary token generated for email: {}", email);
            return token;
        } catch (Exception e) {
            logger.error("Failed to generate token for email {}: {}", email, e.getMessage());
            throw new AppException(ErrorCode.TOKEN_GENERATION_FAILED);
        }
    }
    
    @Override
    public String sendOtp(String email) {
        try {
            // Check if the email exists in the database
            if (!userRepository.existsByEmail(email)) {
                logger.warn("Email not found in the database: {}", email);
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }
            
            // Generate OTP
            String otp = generateOtp(email);
            
            // Send OTP via email
            String subject = "Your One-Time Password (OTP)";
            String message = "Your OTP is: " + otp + ". It will expire in 1 minute.";
            
            sendSimpleMessage(email, subject, message);
            
            logger.info("OTP sent to email: {}", email);
            return otp;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to send OTP to email {}: {}", email, e.getMessage());
            throw new AppException(ErrorCode.EMAIL_SENDING_FAILED);
        }
    }
    
    /**
     * Generates a random numeric OTP of specified length
     * @return The generated OTP
     */
    private String generateRandomOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }
} 