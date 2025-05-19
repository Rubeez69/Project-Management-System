package com.project_management.final_project.config;

import com.project_management.final_project.dto.request.IntrospectRequest;
import com.project_management.final_project.dto.response.IntrospectResponse;
import com.project_management.final_project.exception.AppException;
import com.project_management.final_project.exception.ErrorCode;
import com.project_management.final_project.service.AuthService;
import com.project_management.final_project.service.impl.AuthServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
@Component
public class CustomJwtDecoder implements JwtDecoder{
    @Value("${jwt.secret}")
    private String secret;
    private SecretKey key;
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(CustomJwtDecoder.class);

    public CustomJwtDecoder(AuthService authService, JwtKeyProvider jwtKeyProvider) {
        this.authService = authService;
        this.key = jwtKeyProvider.getKey();
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            logger.info("GEN key: " + Base64.getEncoder().encodeToString(key.getEncoded()));
            // Step 1: Custom introspection check
            IntrospectRequest request = new IntrospectRequest();
            request.setToken(token);
            IntrospectResponse response = authService.introspect(request);
            if (!response.isValid()) {
                throw new AppException(ErrorCode.TOKEN_INVALID);
            }

            Claims claims = authService.validateToken(token);

            // Step 3: Build Spring Security Jwt object
            return new Jwt(
                    token,
                    claims.getIssuedAt() != null ? claims.getIssuedAt().toInstant() : null,
                    claims.getExpiration() != null ? claims.getExpiration().toInstant() : null,
                    Map.of(),
                    new HashMap<>(claims)
            );

        } catch (io.jsonwebtoken.JwtException | AppException e) {
            throw new AppException(ErrorCode.TOKEN_INVALID);
        }
    }
}
