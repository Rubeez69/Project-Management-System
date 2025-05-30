package com.project_management.final_project.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterRequest {
    private String search;
    private List<String> roles;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
} 