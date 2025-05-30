package com.project_management.final_project.repository;

import com.project_management.final_project.entities.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Integer> {
    
    /**
     * Find the top N most recently added team members for a project
     * @param projectId The project ID
     * @param limit The maximum number of members to return
     * @return List of team members
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.project.id = :projectId ORDER BY tm.addedAt DESC")
    List<TeamMember> findTopNByProjectIdOrderByAddedAtDesc(
            @Param("projectId") Integer projectId, 
            org.springframework.data.domain.Pageable pageable);
} 