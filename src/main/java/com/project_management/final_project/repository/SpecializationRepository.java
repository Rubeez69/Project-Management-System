package com.project_management.final_project.repository;

import com.project_management.final_project.entities.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecializationRepository extends JpaRepository<Specialization, Integer> {
    
    /**
     * Find all specializations ordered by name
     * @return List of specializations
     */
    List<Specialization> findAllByOrderByNameAsc();
} 