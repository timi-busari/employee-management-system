package com.employeemgt.employee.service;

import com.employeemgt.employee.model.EmployeeEvent;
import com.employeemgt.employee.entity.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka Producer Service for Employee Events
 * Following GeeksforGeeks Spring Boot Kafka integration guide
 */
@Service
public class EmployeeEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeEventProducer.class);

    @Value("${app.kafka.topic.employee-events:employee-events}")
    private String topicName;

    @Autowired
    private KafkaTemplate<String, EmployeeEvent> kafkaTemplate;

    /**
     * Send employee event to Kafka topic
     */
    public void sendEmployeeEvent(EmployeeEvent event) {
        try {
            // Generate unique event ID if not set
            if (event.getEventId() == null || event.getEventId().isEmpty()) {
                event.setEventId(UUID.randomUUID().toString());
            }

            logger.info("Sending employee event to topic {}: {}", topicName, event);

            // Send message to Kafka topic
            CompletableFuture<SendResult<String, EmployeeEvent>> future = 
                kafkaTemplate.send(topicName, event.getEmployeeId().toString(), event);

            // Handle success callback
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Successfully sent employee event: {} to topic: {} with offset: {}", 
                        event.getEventId(), topicName, result.getRecordMetadata().offset());
                } else {
                    logger.error("Failed to send employee event: {} to topic: {}", 
                        event.getEventId(), topicName, ex);
                }
            });

        } catch (Exception e) {
            logger.error("Error sending employee event to Kafka: {}", event, e);
        }
    }

    /**
     * Helper method to create and send employee created event
     */
    public void sendEmployeeCreatedEvent(Long employeeId, String employeeCode, String firstName, 
                                       String lastName, String email, String department) {
        EmployeeEvent event = new EmployeeEvent(
            UUID.randomUUID().toString(),
            employeeId,
            employeeCode,
            firstName,
            lastName,
            email,
            department,
            "CREATE"
        );
        
        sendEmployeeEvent(event);
    }

    /**
     * Helper method to create and send employee updated event
     */
    public void sendEmployeeUpdatedEvent(Long employeeId, String employeeCode, String firstName, 
                                       String lastName, String email, String department) {
        EmployeeEvent event = new EmployeeEvent(
            UUID.randomUUID().toString(),
            employeeId,
            employeeCode,
            firstName,
            lastName,
            email,
            department,
            "UPDATE"
        );
        
        sendEmployeeEvent(event);
    }

    /**
     * Helper method to create and send employee deleted event
     */
    public void sendEmployeeDeletedEvent(Long employeeId, String employeeCode, String email) {
        EmployeeEvent event = new EmployeeEvent(
            UUID.randomUUID().toString(),
            employeeId,
            employeeCode,
            null, // firstName not needed for delete
            null, // lastName not needed for delete
            email,
            null, // department not needed for delete
            "DELETE"
        );
        
        sendEmployeeEvent(event);
    }

    /**
     * Helper method to create and send employee created event from Employee entity
     */
    public void sendEmployeeCreatedEvent(Employee employee) {
        EmployeeEvent event = new EmployeeEvent(
            UUID.randomUUID().toString(),
            employee.getId(),
            employee.getEmployeeCode(),
            employee.getFirstName(),
            employee.getLastName(),
            employee.getEmail(),
            employee.getDepartment() != null ? employee.getDepartment().getName() : null,
            "CREATE"
        );
        
        sendEmployeeEvent(event);
    }

    /**
     * Helper method to create and send employee updated event from Employee entity
     */
    public void sendEmployeeUpdatedEvent(Employee employee) {
        EmployeeEvent event = new EmployeeEvent(
            UUID.randomUUID().toString(),
            employee.getId(),
            employee.getEmployeeCode(),
            employee.getFirstName(),
            employee.getLastName(),
            employee.getEmail(),
            employee.getDepartment() != null ? employee.getDepartment().getName() : null,
            "UPDATE"
        );
        
        sendEmployeeEvent(event);
    }

    /**
     * Helper method to create and send employee deleted event from Employee entity
     */
    public void sendEmployeeDeletedEvent(Employee employee) {
        EmployeeEvent event = new EmployeeEvent(
            UUID.randomUUID().toString(),
            employee.getId(),
            employee.getEmployeeCode(),
            employee.getFirstName(),
            employee.getLastName(),
            employee.getEmail(),
            employee.getDepartment() != null ? employee.getDepartment().getName() : null,
            "DELETE"
        );
        
        sendEmployeeEvent(event);
    }
}