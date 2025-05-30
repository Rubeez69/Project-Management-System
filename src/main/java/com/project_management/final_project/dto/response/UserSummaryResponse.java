package com.project_management.final_project.dto.response;

import com.project_management.final_project.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {
    private Integer id;
    private String name;
    private String email;
    
    public static UserSummaryResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        
        return UserSummaryResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
} 