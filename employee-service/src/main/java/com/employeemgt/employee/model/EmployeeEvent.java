package com.employeemgt.employee.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Simple Kafka message model for employee events
 * Following GeeksforGeeks Kafka Spring Boot integration guide
 */
public class EmployeeEvent {
    
    @JsonProperty("eventId")
    private String eventId;
    
    @JsonProperty("employeeId")
    private Long employeeId;
    
    @JsonProperty("employeeCode")
    private String employeeCode;
    
    @JsonProperty("firstName")
    private String firstName;
    
    @JsonProperty("lastName")
    private String lastName;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("department")
    private String department;
    
    @JsonProperty("action")
    private String action; // CREATE, UPDATE, DELETE
    
    // Default constructor for JSON deserialization
    public EmployeeEvent() {}
    
    // Constructor
    public EmployeeEvent(String eventId, Long employeeId, String employeeCode, 
                        String firstName, String lastName, String email, 
                        String department, String action) {
        this.eventId = eventId;
        this.employeeId = employeeId;
        this.employeeCode = employeeCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.action = action;
    }
    
    // Getters and Setters
    public String getEventId() {
        return eventId;
    }
    
    public void setEventId(String eventId) {
        this.eventId = eventId;
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
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    @Override
    public String toString() {
        return "EmployeeEvent{" +
                "eventId='" + eventId + '\'' +
                ", employeeId=" + employeeId +
                ", employeeCode='" + employeeCode + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", department='" + department + '\'' +
                ", action='" + action + '\'' +
                '}';
    }
}