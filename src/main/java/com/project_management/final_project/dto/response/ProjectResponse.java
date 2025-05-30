package com.project_management.final_project.dto.response;

import com.project_management.final_project.entities.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Integer id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private UserSummaryResponse createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int teamMembersCount;
    private int tasksCount;
    
    public static ProjectResponse fromEntity(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .status(project.getStatus().name())
                .createdBy(UserSummaryResponse.fromEntity(project.getCreatedBy()))
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .teamMembersCount(project.getTeamMembers() != null ? project.getTeamMembers().size() : 0)
                .tasksCount(project.getTasks() != null ? project.getTasks().size() : 0)
                .build();
    }
    
    public boolean isArchived() {
        return "ARCHIVED".equals(status);
    }
} 