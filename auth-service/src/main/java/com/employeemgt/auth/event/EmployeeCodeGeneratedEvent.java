package com.employeemgt.auth.event;

import java.time.LocalDateTime;

public class EmployeeCodeGeneratedEvent {
    
    private String eventId;
    private String eventType = "EMPLOYEE_CODE_GENERATED";
    private LocalDateTime timestamp;
    
    // Employee details
    private Long employeeId;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    
    // Auth service linking
    private Long userId; // If available during creation
    
    public EmployeeCodeGeneratedEvent() {
        this.timestamp = LocalDateTime.now();
        this.eventId = java.util.UUID.randomUUID().toString();
    }
    
    public EmployeeCodeGeneratedEvent(Long employeeId, String employeeCode, String firstName, 
                                    String lastName, String email, String role) {
        this();
        this.employeeId = employeeId;
        this.employeeCode = employeeCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }

    // Getters and setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "EmployeeCodeGeneratedEvent{" +
                "eventId='" + eventId + '\'' +
                ", employeeCode='" + employeeCode + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}