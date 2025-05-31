package com.project_management.final_project.repository;

import com.project_management.final_project.entities.TeamMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            
    /**
     * Check if a user is already a member of a project
     * @param userId The user ID
     * @param projectId The project ID
     * @return true if the user is already a member of the project, false otherwise
     */
    boolean existsByUserIdAndProjectId(Integer userId, Integer projectId);
    
    /**
     * Find team members by project ID with optional search and specialization filter
     * @param projectId The project ID
     * @param search Optional search term for user name or email
     * @param specializationId Optional specialization ID filter
     * @param pageable Pagination information
     * @return Page of team members
     */
    @Query("SELECT tm FROM TeamMember tm " +
           "WHERE tm.project.id = :projectId " +
           "AND (:search IS NULL OR LOWER(tm.user.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "    OR LOWER(tm.user.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:specializationId IS NULL OR tm.specialization.id = :specializationId)")
    Page<TeamMember> findByProjectIdWithFilters(
            @Param("projectId") Integer projectId,
            @Param("search") String search,
            @Param("specializationId") Integer specializationId,
            Pageable pageable);
} 