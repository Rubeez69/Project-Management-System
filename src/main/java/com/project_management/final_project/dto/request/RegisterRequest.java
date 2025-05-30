package com.project_management.final_project.dto.request;

import com.project_management.final_project.entities.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private User.Gender gender;
} 