package com.employeemgt.employee.service;

import com.employeemgt.employee.dto.EmployeeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EmployeeEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(EmployeeEventPublisher.class);
    private static final String EMPLOYEE_EVENTS_TOPIC = "employee-events";
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    public void publishEmployeeEvent(EmployeeEvent event) {
        try {
            logger.info("Attempting to publish employee event: {} for employee ID: {}", 
                    event.getEventType(), event.getEmployeeId());
                    
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(EMPLOYEE_EVENTS_TOPIC, event.getEmployeeId().toString(), event);
                
            future.whenComplete((result, throwable) -> {
                if (throwable == null) {
                    logger.info("Successfully published employee event: {} with key: {} to partition: {}", 
                            event.getEventType(), event.getEmployeeId(), 
                            result.getRecordMetadata().partition());
                } else {
                    logger.error("Failed to publish employee event: {} with key: {}", 
                            event.getEventType(), event.getEmployeeId(), throwable);
                }
            });
        } catch (Exception e) {
            logger.error("Error publishing employee event: {}", event, e);
        }
    }
    
    public void publishEmployeeCreated(Long employeeId, String firstName, String lastName, 
                                     String email, String departmentName, String userId) {
        EmployeeEvent event = new EmployeeEvent("CREATED", employeeId, firstName, 
                lastName, email, departmentName, userId);
        publishEmployeeEvent(event);
    }
    
    public void publishEmployeeUpdated(Long employeeId, String firstName, String lastName, 
                                     String email, String departmentName, String userId) {
        EmployeeEvent event = new EmployeeEvent("UPDATED", employeeId, firstName, 
                lastName, email, departmentName, userId);
        publishEmployeeEvent(event);
    }
    
    public void publishEmployeeDeleted(Long employeeId, String firstName, String lastName, 
                                     String email, String departmentName, String userId) {
        EmployeeEvent event = new EmployeeEvent("DELETED", employeeId, firstName, 
                lastName, email, departmentName, userId);
        publishEmployeeEvent(event);
    }
}