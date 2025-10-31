package com.employeemgt.employee.controller;

import com.employeemgt.employee.dto.DepartmentRequest;
import com.employeemgt.employee.repository.DepartmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class DepartmentControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DepartmentRepository departmentRepository;

    private MockMvc mockMvc;
    private DepartmentRequest departmentRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Clean up database
        departmentRepository.deleteAll();

        // Create test department request
        departmentRequest = new DepartmentRequest();
        departmentRequest.setName("Engineering");
        departmentRequest.setCode("ENG");
        departmentRequest.setDescription("Software development and engineering operations");
    }

    @Test
    void createDepartment_WithValidData_ShouldCreateAndReturn201() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/departments")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(departmentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Resource created successfully"))
                .andExpect(jsonPath("$.data.name").value("Engineering"))
                .andExpect(jsonPath("$.data.code").value("ENG"))
                .andExpect(jsonPath("$.data.description").value("Software development and engineering operations"));

        // Verify in database
        var departments = departmentRepository.findAll();
        assert departments.size() == 1;
        assert departments.get(0).getName().equals("Engineering");
        assert departments.get(0).getCode().equals("ENG");
    }

    @Test
    void createDepartment_WithDuplicateCode_ShouldReturn409() throws Exception {
        // Create first department
        mockMvc.perform(post("/api/departments")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(departmentRequest)))
                .andExpect(status().isCreated());

        // Try to create another department with same code
        departmentRequest.setName("Human Resources");
        mockMvc.perform(post("/api/departments")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(departmentRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Resource Already Exists"))
                .andExpect(jsonPath("$.message").value("A resource with the provided information already exists"));
    }

    @Test
    void updateDepartment_WithValidData_ShouldUpdateAndReturn200() throws Exception {
        // Create department first
        mockMvc.perform(post("/api/departments")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(departmentRequest)))
                .andExpect(status().isCreated());

        // Get the created department ID
        var department = departmentRepository.findAll().get(0);
        Long departmentId = department.getId();

        // Update department
        departmentRequest.setName("Software Engineering");
        departmentRequest.setDescription("Advanced software development and system architecture");

        mockMvc.perform(put("/api/departments/{id}", departmentId)
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(departmentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Resource updated successfully"))
                .andExpect(jsonPath("$.data.name").value("Software Engineering"))
                .andExpect(jsonPath("$.data.description").value("Advanced software development and system architecture"));

        // Verify in database
        var updatedDepartment = departmentRepository.findById(departmentId).orElse(null);
        assert updatedDepartment != null;
        assert updatedDepartment.getName().equals("Software Engineering");
    }

    @Test
    void deleteDepartment_WithValidId_ShouldDeleteAndReturn204() throws Exception {
        // Create department first
        mockMvc.perform(post("/api/departments")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(departmentRequest)))
                .andExpect(status().isCreated());

        // Get the created department ID
        var department = departmentRepository.findAll().get(0);
        Long departmentId = department.getId();

        // Delete department
        mockMvc.perform(delete("/api/departments/{id}", departmentId)
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isNoContent());

        // Verify in database
        assert departmentRepository.findById(departmentId).isEmpty();
    }

    @Test
    void getDepartmentById_WithValidId_ShouldReturnDepartment() throws Exception {
        // Create department first
        mockMvc.perform(post("/api/departments")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(departmentRequest)))
                .andExpect(status().isCreated());

        // Get the created department ID
        var department = departmentRepository.findAll().get(0);
        Long departmentId = department.getId();

        // Get department by ID
        mockMvc.perform(get("/api/departments/{id}", departmentId)
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Department details retrieved successfully"))
                .andExpect(jsonPath("$.data.name").value("Engineering"))
                .andExpect(jsonPath("$.data.code").value("ENG"));
    }

    @Test
    void getDepartments_ShouldReturnPagedDepartments() throws Exception {
        // Create department first
        mockMvc.perform(post("/api/departments")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(departmentRequest)))
                .andExpect(status().isCreated());

        // Get departments with pagination
        mockMvc.perform(get("/api/departments/all")
                        .header("X-User-Role", "ADMIN")
                        .param("page", "0")
                        .param("perPage", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Engineering"))
                .andExpect(jsonPath("$.meta.total").value(1));
    }

    @Test
    void getDepartments_WithFilters_ShouldReturnFilteredDepartments() throws Exception {
        // Create department first
        mockMvc.perform(post("/api/departments")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(departmentRequest)))
                .andExpect(status().isCreated());

        // Get departments with filters
        mockMvc.perform(get("/api/departments/all")
                        .header("X-User-Role", "ADMIN")
                        .param("name", "Engineering")
                        .param("code", "ENG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Engineering"));
    }
}