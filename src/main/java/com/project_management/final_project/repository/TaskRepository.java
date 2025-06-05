package com.project_management.final_project.repository;

import com.project_management.final_project.entities.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    
    /**
     * Find tasks by project ID
     * @param projectId The project ID
     * @return List of tasks
     */
    List<Task> findByProjectId(Integer projectId);
    
    /**
     * Find tasks assigned to a specific user
     * @param assigneeId The assignee user ID
     * @return List of tasks
     */
    List<Task> findByAssigneeId(Integer assigneeId);
    
    /**
     * Check if a task exists in a project
     * @param taskId The task ID
     * @param projectId The project ID
     * @return true if the task exists in the project, false otherwise
     */
    boolean existsByIdAndProjectId(Integer taskId, Integer projectId);
    
    /**
     * Count tasks assigned to a specific user
     * @param assigneeId The assignee user ID
     * @return Count of tasks assigned to the user
     */
    int countByAssigneeId(Integer assigneeId);
    
    /**
     * Find unassigned tasks with optional filtering by project ID, search term, and priority
     * @param projectId The project ID (optional)
     * @param search Optional search term for task title or description
     * @param priority Optional priority filter
     * @param pageable Pagination information
     * @return Page of unassigned tasks
     */
    @Query("SELECT t FROM Task t " +
           "WHERE t.status = com.project_management.final_project.entities.Task.Status.UNASSIGNED " +
           "AND (:projectId IS NULL OR t.project.id = :projectId) " +
           "AND (:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "    OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:priority IS NULL OR t.priority = :priority)")
    Page<Task> findUnassignedTasksWithFilters(
            @Param("projectId") Integer projectId,
            @Param("search") String search,
            @Param("priority") Task.Priority priority,
            Pageable pageable);
            
    /**
     * Find tasks assigned to a specific user in a specific project with optional filtering
     * @param projectId The project ID
     * @param assigneeId The assignee user ID
     * @param pageable Pagination information
     * @return Page of tasks
     */
    @Query("SELECT t FROM Task t " +
           "WHERE t.project.id = :projectId " +
           "AND t.assignee.id = :assigneeId")
    Page<Task> findTasksByProjectIdAndAssigneeId(
            @Param("projectId") Integer projectId,
            @Param("assigneeId") Integer assigneeId,
            Pageable pageable);
    
    /**
     * Find all tasks assigned to a specific user in a specific project
     * @param projectId The project ID
     * @param assigneeId The assignee user ID
     * @return List of tasks
     */
    @Query("SELECT t FROM Task t " +
           "WHERE t.project.id = :projectId " +
           "AND t.assignee.id = :assigneeId " +
           "ORDER BY t.dueDate ASC")
    List<Task> findAllTasksByProjectIdAndAssigneeId(
            @Param("projectId") Integer projectId,
            @Param("assigneeId") Integer assigneeId);
            
    /**
     * Find tasks that are due within the next 3 days for a specific assignee
     * @param assigneeId The assignee user ID
     * @param currentDate The current date
     * @param dueDateLimit The due date limit (current date + 3 days)
     * @return List of tasks due within the next 3 days
     */
    @Query("SELECT t FROM Task t " +
           "WHERE t.assignee.id = :assigneeId " +
           "AND t.dueDate <= :dueDateLimit " +
           "AND t.dueDate >= :currentDate " +
           "ORDER BY t.dueDate ASC")
    List<Task> findUpcomingDueTasks(
            @Param("assigneeId") Integer assigneeId,
            @Param("currentDate") LocalDate currentDate,
            @Param("dueDateLimit") LocalDate dueDateLimit);
            
    /**
     * Check if a task exists in a project and the project was created by a specific user
     * @param taskId The task ID
     * @param projectId The project ID
     * @param createdById The ID of the user who created the project
     * @return true if the task exists in the project and the project was created by the specified user, false otherwise
     */
    boolean existsByIdAndProjectIdAndProject_CreatedBy_Id(Integer taskId, Integer projectId, Integer createdById);
    
    /**
     * Check if a task exists in a project and is assigned to a specific user
     * @param taskId The task ID
     * @param projectId The project ID
     * @param assigneeId The assignee user ID
     * @return true if the task exists in the project and is assigned to the specified user, false otherwise
     */
    boolean existsByIdAndProjectIdAndAssigneeId(Integer taskId, Integer projectId, Integer assigneeId);
    
    /**
     * Find tasks assigned to a specific user in a specific project
     * @param assigneeId The assignee user ID
     * @param projectId The project ID
     * @return List of tasks
     */
    List<Task> findByAssigneeIdAndProjectId(Integer assigneeId, Integer projectId);
    
    /**
     * Update tasks to unassigned when a team member is removed
     * @param assigneeId The assignee user ID
     * @param projectId The project ID
     * @return Number of tasks updated
     */
    @Modifying
    @Query("UPDATE Task t SET t.assignee = null, t.status = com.project_management.final_project.entities.Task.Status.UNASSIGNED " +
           "WHERE t.assignee.id = :assigneeId AND t.project.id = :projectId")
    int unassignTasksForTeamMember(@Param("assigneeId") Integer assigneeId, @Param("projectId") Integer projectId);
    
    /**
     * Find tasks by project ID with optional filtering by search term, status, and priority
     * @param projectId The project ID
     * @param search Optional search term for task title or description
     * @param status Optional status filter
     * @param priority Optional priority filter
     * @param pageable Pagination information
     * @return Page of tasks
     */
    @Query("SELECT t FROM Task t " +
           "WHERE t.project.id = :projectId " +
           "AND (:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "    OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:priority IS NULL OR t.priority = :priority)")
    Page<Task> findTasksByProjectIdWithFilters(
            @Param("projectId") Integer projectId,
            @Param("search") String search,
            @Param("status") Task.Status status,
            @Param("priority") Task.Priority priority,
            Pageable pageable);
    
    /**
     * Check if a task with the same title already exists in a project
     * @param title The task title
     * @param projectId The project ID
     * @return true if a task with the same title exists in the project, false otherwise
     */
    boolean existsByTitleAndProjectId(String title, Integer projectId);
    
    /**
     * Check if a task with the same title already exists in a project (excluding a specific task)
     * @param title The task title
     * @param projectId The project ID
     * @param taskId The ID of the task to exclude
     * @return true if a task with the same title exists in the project (excluding the specified task), false otherwise
     */
    boolean existsByTitleAndProjectIdAndIdNot(String title, Integer projectId, Integer taskId);
    
    /**
     * Find all tasks in a project ordered by updated at timestamp in descending order
     * @param projectId The project ID
     * @return List of tasks
     */
    List<Task> findAllByProjectIdOrderByUpdatedAtDesc(Integer projectId);
} 