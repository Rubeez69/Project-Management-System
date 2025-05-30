package com.project_management.final_project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class FinalProjectApplication {
	private static final Logger logger = LoggerFactory.getLogger(FinalProjectApplication.class);

	public static void main(String[] args) {
		// Set SSL properties before application starts
		System.setProperty("javax.net.ssl.trustStore", "NONE");
		System.setProperty("com.sun.net.ssl.checkRevocation", "false");
		SpringApplication.run(FinalProjectApplication.class, args);
	}
	
	// One-time utility to print encoded password to logs
	@Bean
	public CommandLineRunner passwordEncoderUtility() {
		return args -> {
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			
			// Add your passwords to encode here
			String[] passwords = {"admin123", "pmanager123", "develop123"};
			
			logger.info("======= ENCODED PASSWORDS =======");
			for (String password : passwords) {
				String encoded = encoder.encode(password);
				logger.info("Original: {}, Encoded: {}", password, encoded);
			}
			logger.info("=================================");
		};
	}
}
