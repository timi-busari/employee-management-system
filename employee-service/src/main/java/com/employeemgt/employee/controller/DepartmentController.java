package com.employeemgt.employee.controller;

import com.employeemgt.employee.dto.ApiResponse;
import com.employeemgt.employee.dto.DepartmentFilterRequest;
import com.employeemgt.employee.dto.DepartmentRequest;
import com.employeemgt.employee.dto.DepartmentResponse;
import com.employeemgt.employee.dto.PaginatedApiResponse;
import com.employeemgt.employee.security.RoleRequired;
import com.employeemgt.employee.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "*")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/all")
    @RoleRequired({ "ADMIN" })
    public ResponseEntity<PaginatedApiResponse<DepartmentResponse>> getDepartments(
            @Valid DepartmentFilterRequest filter) {
        
        Page<DepartmentResponse> departments = departmentService.getDepartmentsWithFilters(filter);
        
        return ResponseEntity.ok(PaginatedApiResponse.of(departments, "Departments retrieved successfully"));
    }

    @GetMapping("/{id}")
    @RoleRequired({"ADMIN"})
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(@PathVariable Long id) {
        DepartmentResponse department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponse.success("Department details retrieved successfully", department));
    }

    @PostMapping
    @RoleRequired({ "ADMIN" })
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
            @Valid @RequestBody DepartmentRequest request) {
        DepartmentResponse department = departmentService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(department));
    }

    @PutMapping("/{id}")
    @RoleRequired({ "ADMIN" })
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(@PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {
        DepartmentResponse department = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(ApiResponse.updated(department));
    }

    @DeleteMapping("/{id}")
    @RoleRequired({ "ADMIN" })
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "employee-service");
        status.put("module", "departments");
        return ResponseEntity.ok(ApiResponse.success("Service is healthy", status));
    }
}