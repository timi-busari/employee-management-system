package com.employeemgt.employee.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO for department filtering and search queries
 */
public class DepartmentFilterRequest {

    // Pagination
    private int page = 0;
    private int perPage = 10;

    // Filters
    private Long id;
    
    @Size(max = 10, message = "Department code cannot exceed 10 characters")
    private String code;
    
    @Size(max = 100, message = "Department name cannot exceed 100 characters")
    private String name;
    
    // Default constructor
    public DepartmentFilterRequest() {}
    
    // Constructor with all fields
    public DepartmentFilterRequest(Long id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }
    
    // Pagination getters and setters
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
    
    // Filter getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    // Helper methods
    public boolean hasId() {
        return id != null;
    }
    
    public boolean hasCode() {
        return code != null && !code.trim().isEmpty();
    }
    
    public boolean hasName() {
        return name != null && !name.trim().isEmpty();
    }
    
    public boolean isEmpty() {
        return !hasId() && !hasCode() && !hasName();
    }
    
    @Override
    public String toString() {
        return "DepartmentFilterRequest{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}