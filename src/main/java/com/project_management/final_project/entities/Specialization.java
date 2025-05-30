package com.project_management.final_project.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "specializations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Specialization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @OneToMany(mappedBy = "specialization", fetch = FetchType.LAZY)
    private List<TeamMember> teamMembers = new ArrayList<>();
} 