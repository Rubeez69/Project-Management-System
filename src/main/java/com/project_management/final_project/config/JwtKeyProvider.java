package com.project_management.final_project.config;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtKeyProvider {
    private final SecretKey key;

    public JwtKeyProvider(@Value("${jwt.secret}") String secret) {
        byte[] keyBytes = hexStringToByteArray(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public SecretKey getKey() {
        return key;
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
