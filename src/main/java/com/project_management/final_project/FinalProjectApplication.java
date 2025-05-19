package com.project_management.final_project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class FinalProjectApplication {
	private static final Logger logger = LoggerFactory.getLogger(FinalProjectApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(FinalProjectApplication.class, args);
	}
	// One-time utility to print encoded password to logs
}
