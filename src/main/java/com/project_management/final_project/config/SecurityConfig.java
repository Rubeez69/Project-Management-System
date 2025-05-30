package com.project_management.final_project.config;

import com.project_management.final_project.service.AuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;

@EnableMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfig {
    private final String[] PUBLIC_ENDPOINTS = {
        "/api/auth/login", 
        "/api/auth/register", 
        "/api/auth/verify", 
        "/api/auth/resendotp", 
        "/api/auth/refresh", 
        "/api/auth/recovery", 
        "/api/auth/recovery/verify-otp", 
        "/api/auth/recovery/newpassword",
        "/api/auth/reset-password",
        "/api/email/otp/send",
        "/api/email/otp/verify",
        "/api/email/token/generate"
    };
    
    private final String[] TEST_ENDPOINTS = {
        "/api/test/**"
    };

    @Bean
    public CustomJwtDecoder customJwtDecoder(AuthService authService, JwtKeyProvider keyProvider) {
        return new CustomJwtDecoder(authService, keyProvider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity, CustomJwtDecoder customJwtDecoder) throws Exception {
        httpSecurity
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(request -> request
                        .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(TEST_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwtConfigurer -> jwtConfigurer
                                .decoder(customJwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                )
                .csrf(AbstractHttpConfigurer::disable);
        return httpSecurity.build();
    }


    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOriginPatterns(Collections.singletonList("*")); // Use this instead of addAllowedOrigin
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.setAllowCredentials(true); // Important for authentication requests

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(urlBasedCorsConfigurationSource);
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new CustomGrantedAuthoritiesConverter());
        return converter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
