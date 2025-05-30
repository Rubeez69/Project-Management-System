package com.project_management.final_project.repository;

import com.project_management.final_project.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
