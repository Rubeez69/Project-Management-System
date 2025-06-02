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
public class TeamMemberWithWorkloadResponse {
    private Integer id;
    private Integer userId;
    private String profile;
    private String name;
    private String email;
    private String specialization;
    private int workload; // Number of tasks assigned to this member
    
    public static TeamMemberWithWorkloadResponse fromEntityWithWorkload(TeamMember teamMember, int taskCount) {
        if (teamMember == null) {
            return null;
        }
        
        return TeamMemberWithWorkloadResponse.builder()
                .id(teamMember.getId())
                .userId(teamMember.getUser() != null ? teamMember.getUser().getId() : null)
                .profile(teamMember.getUser() != null ? teamMember.getUser().getProfile() : null)
                .name(teamMember.getUser() != null ? teamMember.getUser().getName() : null)
                .email(teamMember.getUser() != null ? teamMember.getUser().getEmail() : null)
                .specialization(teamMember.getSpecialization() != null ? 
                        teamMember.getSpecialization().getName() : null)
                .workload(taskCount)
                .build();
    }
} 