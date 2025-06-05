package com.project_management.final_project.repository;

import com.project_management.final_project.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    List<User> findByGender(User.Gender gender);
    
    List<User> findByDateOfBirthBetween(LocalDate startDate, LocalDate endDate);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Find users by roles and search term
     * @param roleNames List of role names to include
     * @param excludedRoleNames List of role names to exclude
     * @param searchTerm Search term for name or email (optional)
     * @param pageable Pagination and sorting
     * @return Page of users
     */
    @Query("SELECT u FROM User u JOIN u.role r WHERE " +
           "r.name IN :roleNames AND " +
           "r.name NOT IN :excludedRoleNames AND " +
           "(:searchTerm IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "u.status = 'ACTIVE'")
    Page<User> findByRolesAndSearchTerm(
            @Param("roleNames") List<String> roleNames,
            @Param("excludedRoleNames") List<String> excludedRoleNames,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);
            
    /**
     * Find users by roles and search term who are not already members of a specific project
     * @param roleNames List of role names to include
     * @param excludedRoleNames List of role names to exclude
     * @param searchTerm Search term for name or email (optional)
     * @param projectId Project ID to exclude team members from
     * @param pageable Pagination and sorting
     * @return Page of users
     */
    @Query("SELECT u FROM User u JOIN u.role r WHERE " +
           "r.name IN :roleNames AND " +
           "r.name NOT IN :excludedRoleNames AND " +
           "(:searchTerm IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "u.status = 'ACTIVE' AND " +
           "NOT EXISTS (SELECT tm FROM TeamMember tm WHERE tm.user.id = u.id AND tm.project.id = :projectId)")
    Page<User> findByRolesAndSearchTermNotInProject(
            @Param("roleNames") List<String> roleNames,
            @Param("excludedRoleNames") List<String> excludedRoleNames,
            @Param("searchTerm") String searchTerm,
            @Param("projectId") Integer projectId,
            Pageable pageable);
}
