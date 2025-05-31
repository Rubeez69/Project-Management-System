package com.project_management.final_project.service.impl;

import com.project_management.final_project.config.JwtKeyProvider;
import com.project_management.final_project.dto.request.AuthRequest;
import com.project_management.final_project.dto.request.IntrospectRequest;
import com.project_management.final_project.dto.request.ResetPasswordRequest;
import com.project_management.final_project.dto.response.AuthResponse;
import com.project_management.final_project.dto.response.IntrospectResponse;
import com.project_management.final_project.dto.response.PermissionResponse;
import com.project_management.final_project.entities.User;
import com.project_management.final_project.exception.AppException;
import com.project_management.final_project.exception.ErrorCode;
import com.project_management.final_project.repository.UserRepository;
import com.project_management.final_project.service.AuthService;
import com.project_management.final_project.util.ValidationUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    private SecretKey key;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtKeyProvider jwtKeyProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.key = jwtKeyProvider.getKey();
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest request) {
        var token = request.getToken();
        boolean isValid = true;
        try {
            validateToken(token);
        } catch (AppException e) {
            isValid = false;
            logger.error("Failed to verify token, input token: {}. Error: {}", token, e.getMessage());
        }
        IntrospectResponse response = new IntrospectResponse(isValid);
        return response;
    }

    @Override
    public String generateAccessToken(User user) {
        try {
            logger.info("GEN key: " + Base64.getEncoder().encodeToString(key.getEncoded()));
            return Jwts.builder()
                    .claims()
                    .add("id", user.getId())
                    .add("email", user.getEmail())
                    .add("role", user.getRole().getName())
                    .add("permissions", user.getRole().getPermissions().stream()
                            .map(p -> new PermissionResponse(
                                    p.getModule().getName(),
                                    p.isCanView(),
                                    p.isCanCreate(),
                                    p.isCanUpdate(),
                                    p.isCanDelete()
                            ))
                            .collect(Collectors.toList()))
                    .subject(user.getEmail())
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                    .and()
                    .signWith(key, Jwts.SIG.HS512)
                    .compact();
        } catch (Exception e) {
            throw new AppException(ErrorCode.TOKEN_GENERATION_FAILED);
        }
    }

    @Override
    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .claims()
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .and()
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }

    @Override
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        } catch (MalformedJwtException e) {
            throw new AppException(ErrorCode.TOKEN_MALFORMED);
        } catch (SecurityException e) {
            throw new AppException(ErrorCode.TOKEN_SIGNATURE_INVALID);
        } catch (Exception e) {
            throw new AppException(ErrorCode.TOKEN_INVALID);
        }
    }

    @Override
    public String refreshAccessToken(String refreshToken) {
        Claims claims = validateToken(refreshToken);
        // 1. Get the subject (email or userId)
        String email = claims.getSubject();

        // 2. Load user from DB
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 3. Generate a new access token
        return generateAccessToken(user);
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        // Validate email format
        if (!ValidationUtil.isValidEmail(request.getEmail())) {
            logger.warn("Invalid email format: {}", request.getEmail());
            throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT);
        }
        
        // Validate password format (only for registration, not for login)
        // For login, we just check if the user exists and if the password matches
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.ACCOUNT_INVALID);
        }
        
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken);
    }
 
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        try {
            // Validate the token
            Claims claims = validateToken(request.getToken());
            
            // Extract email from token
            String email = claims.get("email", String.class);
            if (email == null) {
                logger.warn("Email not found in token");
                throw new AppException(ErrorCode.TOKEN_INVALID);
            }
            
            // Check token type
            String tokenType = claims.get("type", String.class);
            if (!"OTP_VERIFICATION".equals(tokenType)) {
                logger.warn("Invalid token type: {}", tokenType);
                throw new AppException(ErrorCode.TOKEN_INVALID);
            }
            
            // Validate new password format
            if (!ValidationUtil.isValidPassword(request.getNewPassword())) {
                logger.warn("Invalid password format");
                throw new AppException(ErrorCode.INVALID_PASSWORD_FORMAT);
            }
            
            // Find user by email
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            
            // Update password
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            
            logger.info("Password reset successful for user: {}", email);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Password reset failed: {}", e.getMessage());
            throw new AppException(ErrorCode.PASSWORD_RESET_FAILED);
        }
    }
}
