package com.project_management.final_project.service.impl;

import com.project_management.final_project.config.SecurityUtil;
import com.project_management.final_project.dto.request.AddTeamMemberRequest;
import com.project_management.final_project.entities.Project;
import com.project_management.final_project.entities.Specialization;
import com.project_management.final_project.entities.TeamMember;
import com.project_management.final_project.entities.User;
import com.project_management.final_project.exception.AppException;
import com.project_management.final_project.exception.ErrorCode;
import com.project_management.final_project.repository.ProjectRepository;
import com.project_management.final_project.repository.SpecializationRepository;
import com.project_management.final_project.repository.TeamMemberRepository;
import com.project_management.final_project.repository.UserRepository;
import com.project_management.final_project.service.TeamMemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TeamMemberServiceImpl implements TeamMemberService {

    private static final Logger logger = LoggerFactory.getLogger(TeamMemberServiceImpl.class);
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final SpecializationRepository specializationRepository;
    private final SecurityUtil securityUtil;

    @Autowired
    public TeamMemberServiceImpl(
            ProjectRepository projectRepository, 
            UserRepository userRepository, 
            TeamMemberRepository teamMemberRepository,
            SpecializationRepository specializationRepository,
            SecurityUtil securityUtil) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.specializationRepository = specializationRepository;
        this.securityUtil = securityUtil;
    }

    @Override
    @Transactional
    public int addTeamMembers(Integer projectId, List<AddTeamMemberRequest> requests) {
        try {
            // Get current user ID
            Integer currentUserId = securityUtil.getCurrentUserId();
            
            // Find the project by ID
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Project not found with ID: " + projectId));
            
            // Check if the current user is the creator of the project
            if (!project.getCreatedBy().getId().equals(currentUserId)) {
                logger.warn("User ID {} attempted to add members to project ID {} created by user ID {}", 
                        currentUserId, projectId, project.getCreatedBy().getId());
                throw new AppException(ErrorCode.UNAUTHORIZED, "You are not authorized to add members to this project");
            }
            
            List<TeamMember> teamMembersToAdd = new ArrayList<>();
            
            for (AddTeamMemberRequest request : requests) {
                // Check if user exists
                User user = userRepository.findById(request.getUserId())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, 
                                "User not found with ID: " + request.getUserId()));
                
                // Check if specialization exists
                Specialization specialization = specializationRepository.findById(request.getSpecializationId())
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, 
                                "Specialization not found with ID: " + request.getSpecializationId()));
                
                // Check if user is already a member of the project
                boolean isAlreadyMember = teamMemberRepository.existsByUserIdAndProjectId(
                        request.getUserId(), projectId);
                
                if (isAlreadyMember) {
                    logger.warn("User ID {} is already a member of project ID {}", 
                            request.getUserId(), projectId);
                    continue; // Skip this user and continue with the next
                }
                
                // Create new team member
                TeamMember teamMember = TeamMember.builder()
                        .user(user)
                        .project(project)
                        .specialization(specialization)
                        .build();
                
                teamMembersToAdd.add(teamMember);
            }
            
            // Save all team members
            List<TeamMember> savedTeamMembers = teamMemberRepository.saveAll(teamMembersToAdd);
            
            logger.info("Added {} team members to project ID {}", savedTeamMembers.size(), projectId);
            
            return savedTeamMembers.size();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error adding team members to project ID {}: {}", projectId, e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to add team members to project");
        }
    }
} 