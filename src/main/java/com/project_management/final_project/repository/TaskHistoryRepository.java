package com.project_management.final_project.repository;

import com.project_management.final_project.entities.TaskHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Integer> {
    
    /**
     * Find task history records by task ID
     * @param taskId The task ID
     * @return List of task history records
     */
    List<TaskHistory> findByTaskIdOrderByChangedAtDesc(Integer taskId);
    
    /**
     * Find the most recent task history records
     * @param pageable Pagination information including limit
     * @return List of most recent task history records
     */
    @Query("SELECT th FROM TaskHistory th JOIN FETCH th.task JOIN FETCH th.changedBy ORDER BY th.changedAt DESC")
    List<TaskHistory> findMostRecentTaskHistory(Pageable pageable);
    
    /**
     * Find the most recent task history records for tasks that belong to projects created by a specific user
     * @param userId The ID of the user who created the projects
     * @param pageable Pagination information including limit
     * @return List of most recent task history records for tasks in projects created by the user
     */
    @Query("SELECT th FROM TaskHistory th JOIN FETCH th.task t JOIN FETCH th.changedBy " +
           "WHERE t.project.createdBy.id = :userId ORDER BY th.changedAt DESC")
    List<TaskHistory> findMostRecentTaskHistoryForProjectsCreatedByUser(@Param("userId") Integer userId, Pageable pageable);
    
    /**
     * Find the most recent task history records for tasks assigned to a specific user
     * @param userId The user ID
     * @param pageable Pagination information including limit
     * @return List of most recent task history records for the user's assigned tasks
     */
    @Query("SELECT th FROM TaskHistory th JOIN FETCH th.task t JOIN FETCH th.changedBy " +
           "WHERE t.assignee.id = :userId ORDER BY th.changedAt DESC")
    List<TaskHistory> findMostRecentTaskHistoryForUser(@Param("userId") Integer userId, Pageable pageable);
} 