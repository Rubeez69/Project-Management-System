package com.project_management.final_project.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"role_id", "module_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @Column(name = "can_view", nullable = false)
    private boolean canView;

    @Column(name = "can_create", nullable = false)
    private boolean canCreate;

    @Column(name = "can_update", nullable = false)
    private boolean canUpdate;

    @Column(name = "can_delete", nullable = false)
    private boolean canDelete;
}
