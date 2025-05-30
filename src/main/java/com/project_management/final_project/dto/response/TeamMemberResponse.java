package com.project_management.final_project.dto.response;

import com.project_management.final_project.entities.TeamMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberResponse {
    private Integer id;
    private String name;
    private String email;
    private String specialization;
    
    public static TeamMemberResponse fromEntity(TeamMember teamMember) {
        if (teamMember == null) {
            return null;
        }
        
        return TeamMemberResponse.builder()
                .id(teamMember.getId())
                .name(teamMember.getUser() != null ? teamMember.getUser().getName() : null)
                .email(teamMember.getUser() != null ? teamMember.getUser().getEmail() : null)
                .specialization(teamMember.getSpecialization() != null ? 
                        teamMember.getSpecialization().getName() : null)
                .build();
    }
} 