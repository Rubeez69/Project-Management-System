package com.project_management.final_project.dto.response;

import com.project_management.final_project.entities.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDetailResponse {
    private Integer id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private List<TeamMemberResponse> teamMembers;
    
    public static ProjectDetailResponse fromEntity(Project project, List<TeamMemberResponse> teamMembers) {
        return ProjectDetailResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .status(project.getStatus().name())
                .teamMembers(teamMembers)
                .build();
    }
} 