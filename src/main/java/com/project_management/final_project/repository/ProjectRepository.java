package com.project_management.final_project.repository;

import com.project_management.final_project.entities.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    
    @Query("SELECT p FROM Project p WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:status IS NULL OR p.status = :status) " +
           "AND (:createdById IS NULL OR p.createdBy.id = :createdById)")
    Page<Project> findProjectsByFilters(
            @Param("name") String name, 
            @Param("status") Project.Status status, 
            @Param("createdById") Integer createdById,
            Pageable pageable);
    
    Page<Project> findByCreatedById(Integer createdById, Pageable pageable);
} 