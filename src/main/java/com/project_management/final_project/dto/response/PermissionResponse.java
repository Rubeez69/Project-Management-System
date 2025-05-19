package com.project_management.final_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PermissionResponse {
    private String module;
    private boolean canView;
    private boolean canCreate;
    private boolean canUpdate;
    private boolean canDelete;
}
