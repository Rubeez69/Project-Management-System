package com.project_management.final_project.service;

import com.project_management.final_project.dto.request.AuthRequest;
import com.project_management.final_project.dto.request.IntrospectRequest;
import com.project_management.final_project.dto.response.AuthResponse;
import com.project_management.final_project.dto.response.IntrospectResponse;
import com.project_management.final_project.entities.User;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

public interface AuthService {
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    Claims validateToken(String token);
    String refreshAccessToken(String refreshToken);
    AuthResponse login(AuthRequest request);
    IntrospectResponse introspect(IntrospectRequest request);
}
