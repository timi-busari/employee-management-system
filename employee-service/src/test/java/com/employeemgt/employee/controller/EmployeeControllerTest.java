package com.employeemgt.employee.controller;

import com.employeemgt.employee.dto.EmployeeRequest;
import com.employeemgt.employee.entity.Department;
import com.employeemgt.employee.entity.Employee.EmployeeStatus;
import com.employeemgt.employee.repository.DepartmentRepository;
import com.employeemgt.employee.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@Transactional
class EmployeeControllerTest {

        @Autowired
        private WebApplicationContext webApplicationContext;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private EmployeeRepository employeeRepository;

        @Autowired
        private DepartmentRepository departmentRepository;

        private MockMvc mockMvc;
        private Department testDepartment;
        private EmployeeRequest employeeRequest;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

                // Create test department
                testDepartment = new Department();
                testDepartment.setName("Engineering");
                testDepartment.setCode("ENG");
                testDepartment.setDescription("Engineering Department");
                testDepartment = departmentRepository.save(testDepartment);

                // Setup employee request
                employeeRequest = new EmployeeRequest();
                employeeRequest.setEmployeeCode("EMP001");
                employeeRequest.setFirstName("John");
                employeeRequest.setLastName("Doe");
                employeeRequest.setEmail("john.doe@company.com");
                employeeRequest.setPhoneNumber("1234567890");
                employeeRequest.setHireDate(LocalDate.now());
                employeeRequest.setJobTitle("Software Engineer");
                employeeRequest.setSalary(new BigDecimal("75000.00"));
                employeeRequest.setStatus(EmployeeStatus.ACTIVE);
                employeeRequest.setDepartmentId(testDepartment.getId());
        }

        @Test
        void createEmployee_WithValidData_ShouldCreateAndReturn201() throws Exception {
                // Act & Assert
                mockMvc.perform(post("/api/employees").header("X-User-Role", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(employeeRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Employee created successfully"))
                                .andExpect(jsonPath("$.data.firstName").value("John"))
                                .andExpect(jsonPath("$.data.lastName").value("Doe"))
                                .andExpect(jsonPath("$.data.employeeCode").value("EMP001"))
                                .andExpect(jsonPath("$.data.department.name").value("Engineering"));

                // Verify in database
                var employees = employeeRepository.findAll();
                assert employees.size() == 1;
                assert employees.get(0).getFirstName().equals("John");
                assert employees.get(0).getEmployeeCode().equals("EMP001");
        }

        @Test
        void createEmployee_WithDuplicateEmail_ShouldReturn409() throws Exception {
                // Create first employee
                mockMvc.perform(post("/api/employees").header("X-User-Role", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(employeeRequest)))
                                .andExpect(status().isCreated());

                // Try to create another employee with same email
                employeeRequest.setEmployeeCode("EMP002");
                // Then
                mockMvc.perform(post("/api/employees").header("X-User-Role", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(employeeRequest)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.status").value(409))
                                .andExpect(jsonPath("$.error").value("Resource Already Exists"))
                                .andExpect(jsonPath("$.message")
                                                .value("A resource with the provided information already exists"));
        }

        @Test
        void createEmployee_WithDuplicateEmployeeCode_ShouldReturn409() throws Exception {
                // Create first employee
                mockMvc.perform(post("/api/employees").header("X-User-Role", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(employeeRequest)))
                                .andExpect(status().isCreated());

                // Try to create another employee with same employee code but different email
                employeeRequest.setEmail("jane.doe@company.com");
                mockMvc.perform(post("/api/employees").header("X-User-Role", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(employeeRequest)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.status").value(409))
                                .andExpect(jsonPath("$.error").value("Resource Already Exists"));
        }

        @Test
        void createEmployee_WithInvalidData_ShouldReturn400() throws Exception {
                // Test with empty first name
                employeeRequest.setFirstName("");
                
                mockMvc.perform(post("/api/employees").header("X-User-Role", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(employeeRequest)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void createEmployee_WithInvalidEmail_ShouldReturn400() throws Exception {
                // Test with invalid email format
                employeeRequest.setEmail("invalid-email");
                
                mockMvc.perform(post("/api/employees").header("X-User-Role", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(employeeRequest)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldUpdateEmployeeSuccessfullyWhenValidDataProvided() throws Exception {
                // Given - Create employee first
                mockMvc.perform(post("/api/employees").header("X-User-Role", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(employeeRequest)))
                                .andExpect(status().isCreated());

                // And - Get the created employee ID
                var employee = employeeRepository.findAll().get(0);
                Long employeeId = employee.getId();

                // When - Update employee
                employeeRequest.setFirstName("Jane");
                employeeRequest.setJobTitle("Senior Software Engineer");

                // Then
                mockMvc.perform(put("/api/employees/{id}", employeeId)
                                .header("X-User-Role", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(employeeRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Employee updated successfully"))
                                .andExpect(jsonPath("$.data.firstName").value("Jane"))
                                .andExpect(jsonPath("$.data.jobTitle").value("Senior Software Engineer"));

                // And - Verify in database
                var updatedEmployee = employeeRepository.findById(employeeId).orElseThrow();
                assert updatedEmployee.getFirstName().equals("Jane");
                assert updatedEmployee.getJobTitle().equals("Senior Software Engineer");
        }

        @Test
        void updateEmployee_WithNonExistentId_ShouldReturn404() throws Exception {
                Long nonExistentId = 999L;

                mockMvc.perform(put("/api/employees/{id}", nonExistentId)
                                .header("X-User-Role", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(employeeRequest)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.error").value("Resource Not Found"));
        }

        @Test
        void updateEmployee_WithInvalidData_ShouldReturn400() throws Exception {
                // Create employee first
                mockMvc.perform(post("/api/employees").header("X-User-Role", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(employeeRequest)))
                                .andExpect(status().isCreated());

                var employee = employeeRepository.findAll().get(0);
                Long employeeId = employee.getId();

                // Try to update with invalid data
                employeeRequest.setFirstName(""); // Invalid - empty first name

                mockMvc.perform(put("/api/employees/{id}", employeeId)
                                .header("X-User-Role", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(employeeRequest)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldDeleteEmployeeSuccessfullyWhenValidIdProvided() throws Exception {
                // Given - Create employee first
                mockMvc.perform(post("/api/employees").header("X-User-Role", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(employeeRequest)))
                                .andExpect(status().isCreated());

                // And - Get the created employee ID
                var employee = employeeRepository.findAll().get(0);
                Long employeeId = employee.getId();

                // When - Delete employee
                mockMvc.perform(delete("/api/employees/{id}", employeeId)
                                .header("X-User-Role", "ADMIN"))
                                .andExpect(status().isOk());

                // Then - Verify in database
                assert employeeRepository.findById(employeeId).isEmpty();
        }

        @Test
        void deleteEmployee_WithNonExistentId_ShouldReturn404() throws Exception {
                Long nonExistentId = 999L;

                mockMvc.perform(delete("/api/employees/{id}", nonExistentId)
                                .header("X-User-Role", "ADMIN"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.error").value("Resource Not Found"));
        }

        @Test
        void shouldReturnEmployeeWhenValidIdProvided() throws Exception {
                // Given - Create employee first
                mockMvc.perform(post("/api/employees").header("X-User-Role", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(employeeRequest)))
                                .andExpect(status().isCreated());

                // And - Get the created employee ID
                var employee = employeeRepository.findAll().get(0);
                Long employeeId = employee.getId();

                // When & Then - Get employee by ID
                mockMvc.perform(get("/api/employees/{id}", employeeId)
                                .header("X-User-Role", "ADMIN"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.firstName").value("John"))
                                .andExpect(jsonPath("$.data.lastName").value("Doe"))
                                .andExpect(jsonPath("$.data.employeeCode").value("EMP001"));
        }

        @Test
        void getEmployeeById_WithNonExistentId_ShouldReturn404() throws Exception {
                Long nonExistentId = 999L;

                mockMvc.perform(get("/api/employees/{id}", nonExistentId)
                                .header("X-User-Role", "ADMIN"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.error").value("Resource Not Found"));
        }

        @Test
        void getAllEmployees_WithNoEmployees_ShouldReturnEmptyList() throws Exception {
                // Clean up any existing employees
                employeeRepository.deleteAll();

                mockMvc.perform(get("/api/employees/all")
                                .header("X-User-Role", "ADMIN")
                                .param("page", "0")
                                .param("perPage", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data").isEmpty())
                                .andExpect(jsonPath("$.meta.total").value(0));
        }

        @Test
        void getAllEmployees_WithMultipleEmployees_ShouldReturnPagedResults() throws Exception {
                // Create first employee
                mockMvc.perform(post("/api/employees").header("X-User-Role", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(employeeRequest)))
                                .andExpect(status().isCreated());

                // Create second employee
                employeeRequest.setEmployeeCode("EMP002");
                employeeRequest.setEmail("jane.doe@company.com");
                employeeRequest.setFirstName("Jane");
                mockMvc.perform(post("/api/employees").header("X-User-Role", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(employeeRequest)))
                                .andExpect(status().isCreated());

                // Get all employees
                mockMvc.perform(get("/api/employees/all")
                                .header("X-User-Role", "ADMIN")
                                .param("page", "0")
                                .param("perPage", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data.length()").value(2))
                                .andExpect(jsonPath("$.meta.total").value(2));
        }

        @Test
        void getEmployees_WithFilters_ShouldReturnFilteredResults() throws Exception {
                // Create first employee
                mockMvc.perform(post("/api/employees").header("X-User-Role", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(employeeRequest)))
                                .andExpect(status().isCreated());

                // Create second employee in different department
                EmployeeRequest secondEmployeeRequest = new EmployeeRequest();
                secondEmployeeRequest.setEmployeeCode("EMP002");
                secondEmployeeRequest.setFirstName("Jane");
                secondEmployeeRequest.setLastName("Smith");
                secondEmployeeRequest.setEmail("jane.smith@company.com");
                secondEmployeeRequest.setPhoneNumber("0987654321");
                secondEmployeeRequest.setHireDate(LocalDate.now());
                secondEmployeeRequest.setJobTitle("Data Analyst");
                secondEmployeeRequest.setSalary(new BigDecimal("65000.00"));
                secondEmployeeRequest.setStatus(EmployeeStatus.INACTIVE);
                secondEmployeeRequest.setDepartmentId(testDepartment.getId());

                mockMvc.perform(post("/api/employees").header("X-User-Role", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(secondEmployeeRequest)))
                                .andExpect(status().isCreated());

                // Filter by status - should return only ACTIVE employees
                mockMvc.perform(get("/api/employees")
                                .header("X-User-Role", "ADMIN")
                                .param("status", "ACTIVE")
                                .param("page", "0")
                                .param("perPage", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data.length()").value(1))
                                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
        }

        @Test
        void getEmployees_WithDepartmentFilter_ShouldReturnDepartmentEmployees() throws Exception {
                // Create employee
                mockMvc.perform(post("/api/employees").header("X-User-Role", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(employeeRequest)))
                                .andExpect(status().isCreated());

                // Filter by department
                mockMvc.perform(get("/api/employees")
                                .header("X-User-Role", "ADMIN")
                                .param("departmentId", testDepartment.getId().toString())
                                .param("page", "0")
                                .param("perPage", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data.length()").value(1))
                                .andExpect(jsonPath("$.data[0].department.id").value(testDepartment.getId().intValue()));
        }

        @Test
        void getEmployees_WithNameFilter_ShouldReturnMatchingEmployees() throws Exception {
                // Create employee
                mockMvc.perform(post("/api/employees").header("X-User-Role", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(employeeRequest)))
                                .andExpect(status().isCreated());

                // Filter by name
                mockMvc.perform(get("/api/employees")
                                .header("X-User-Role", "ADMIN")
                                .param("name", "John")
                                .param("page", "0")
                                .param("perPage", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data.length()").value(1))
                                .andExpect(jsonPath("$.data[0].firstName").value("John"));
        }

        @Test
        void getEmployees_ShouldReturnPagedEmployees() throws Exception {
                // Create employee first
                mockMvc.perform(post("/api/employees").header("X-User-Role", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(employeeRequest)))
                                .andExpect(status().isCreated());

                // Get employees with pagination
                mockMvc.perform(get("/api/employees/all").header("X-User-Role", "ADMIN")
                                .param("page", "0")
                                .param("perPage", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data[0].firstName").value("John"))
                                .andExpect(jsonPath("$.meta.total").value(1));
        }

        @Test
        void getEmployees_WithFilters_ShouldReturnFilteredEmployees() throws Exception {
                // Create employee first
                mockMvc.perform(post("/api/employees").header("X-User-Role", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(employeeRequest)))
                                .andExpect(status().isCreated());

                // Get employees with filters
                mockMvc.perform(get("/api/employees/all").header("X-User-Role", "ADMIN")
                                .param("departmentId", testDepartment.getId().toString())
                                .param("status", "ACTIVE")
                                .param("name", "John"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data[0].firstName").value("John"));
        }
}