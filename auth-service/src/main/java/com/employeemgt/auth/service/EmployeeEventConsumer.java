package com.employeemgt.auth.service;

import com.employeemgt.auth.entity.Role;
import com.employeemgt.auth.entity.User;
import com.employeemgt.auth.model.EmployeeEvent;
import com.employeemgt.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Kafka Consumer Service for Employee Events
 * Following GeeksforGeeks Spring Boot Kafka integration guide
 */
@Service
public class EmployeeEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeEventConsumer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @KafkaListener(topics = "${app.kafka.topic.employee-events:employee-events}", 
                   groupId = "${spring.kafka.consumer.group-id:auth-service-group}")
    public void handleEmployeeEvent(@Payload EmployeeEvent event,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                   @Header(KafkaHeaders.OFFSET) long offset,
                                   Acknowledgment acknowledgment) {
        
        logger.info("Received employee event from topic: {}, partition: {}, offset: {}, event: {}", 
                   topic, partition, offset, event);

        try {
            switch (event.getAction().toUpperCase()) {
                case "CREATE":
                    handleEmployeeCreated(event);
                    break;
                case "UPDATE":
                    handleEmployeeUpdated(event);
                    break;
                case "DELETE":
                    handleEmployeeDeleted(event);
                    break;
                default:
                    logger.warn("Unknown action in employee event: {}", event.getAction());
            }

            // Manually acknowledge the message after successful processing
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
            
            logger.info("Successfully processed employee event: {}", event.getEventId());

        } catch (Exception e) {
            logger.error("Error processing employee event: {}", event, e);
            // Note: In production, you might want to implement retry logic or dead letter queue
        }
    }

    private void handleEmployeeCreated(EmployeeEvent event) {
        logger.info("Processing employee created event: {}", event.getEventId());

        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmail(event.getEmail());
        
        if (existingUser.isPresent()) {
            // Update existing user with employee details
            User user = existingUser.get();
            user.setEmployeeCode(event.getEmployeeCode());
            user.setFirstName(event.getFirstName());
            user.setLastName(event.getLastName());
            userRepository.save(user);
            
            logger.info("Updated existing user {} with employee code: {}", 
                       event.getEmail(), event.getEmployeeCode());
        } else {
            // Create new user account for the employee
            createUserForEmployee(event);
            logger.info("Created new user account for employee: {}", event.getEmployeeCode());
        }
    }

    private void handleEmployeeUpdated(EmployeeEvent event) {
        logger.info("Processing employee updated event: {}", event.getEventId());

        Optional<User> userOpt = userRepository.findByEmployeeCode(event.getEmployeeCode());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFirstName(event.getFirstName());
            user.setLastName(event.getLastName());
            user.setEmail(event.getEmail());
            userRepository.save(user);
            
            logger.info("Updated user details for employee code: {}", event.getEmployeeCode());
        } else {
            logger.warn("User not found for employee code: {}", event.getEmployeeCode());
        }
    }

    private void handleEmployeeDeleted(EmployeeEvent event) {
        logger.info("Processing employee deleted event: {}", event.getEventId());

        Optional<User> userOpt = userRepository.findByEmployeeCode(event.getEmployeeCode());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Instead of deleting, we'll disable the user account
            user.setEnabled(false);
            user.setEmployeeCode(null); // Remove employee code reference
            userRepository.save(user);
            
            logger.info("Disabled user account for deleted employee: {}", event.getEmployeeCode());
        } else {
            logger.warn("User not found for deleted employee code: {}", event.getEmployeeCode());
        }
    }

    private void createUserForEmployee(EmployeeEvent event) {
        try {
            // Generate username from email
            String username = event.getEmail().substring(0, event.getEmail().indexOf("@"));
            
            // Create new user
            User user = new User();
            user.setUsername(username);
            user.setEmail(event.getEmail());
            user.setFirstName(event.getFirstName());
            user.setLastName(event.getLastName());
            user.setEmployeeCode(event.getEmployeeCode());
            user.setRole(Role.USER); // Default role
            user.setEnabled(false); // Disabled until employee activates account
            
            // Set default password (employee should change on first login)
            String defaultPassword = "TempPass123!";
            user.setPassword(passwordEncoder.encode(defaultPassword));
            
            userRepository.save(user);
            
            logger.info("Created user account for employee: {} with username: {}", 
                       event.getEmployeeCode(), username);
            
        } catch (Exception e) {
            logger.error("Error creating user for employee: {}", event, e);
            throw e;
        }
    }
}