package com.employeemgt.employee.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class EmployeeFilterRequest {
    
    // Pagination
    private int page = 0;
    private int perPage = 10;
    
    // Filters
    private String employeeNumber;
    private Long departmentId;
    private String status;
    private Long managerId;
    private String name;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate hireDateFrom;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate hireDateTo;
    
    // Constructors
    public EmployeeFilterRequest() {}
    
    // Getters and Setters
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }

    public int getPerPage() {
        return perPage;
    }

    public void setPerPage(int perPage) {
        this.perPage = perPage;
    }
    
    public String getEmployeeNumber() {
        return employeeNumber;
    }
    
    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }
    
    public Long getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Long getManagerId() {
        return managerId;
    }
    
    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public LocalDate getHireDateFrom() {
        return hireDateFrom;
    }
    
    public void setHireDateFrom(LocalDate hireDateFrom) {
        this.hireDateFrom = hireDateFrom;
    }
    
    public LocalDate getHireDateTo() {
        return hireDateTo;
    }
    
    public void setHireDateTo(LocalDate hireDateTo) {
        this.hireDateTo = hireDateTo;
    }
}