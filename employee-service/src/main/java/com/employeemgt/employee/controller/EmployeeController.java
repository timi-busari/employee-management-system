package com.employeemgt.employee.controller;

import com.employeemgt.employee.dto.ApiResponse;
import com.employeemgt.employee.dto.EmployeeFilterRequest;
import com.employeemgt.employee.dto.EmployeeRequest;
import com.employeemgt.employee.dto.EmployeeResponse;
import com.employeemgt.employee.dto.PaginatedApiResponse;
import com.employeemgt.employee.security.RoleRequired;
import com.employeemgt.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping
    @RoleRequired({ "ADMIN" })
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse employee = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Employee created successfully", employee));
    }

    @PutMapping("/{id}")
    @RoleRequired({ "ADMIN" })
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {

        EmployeeResponse employee = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", employee));
    }

    @DeleteMapping("/{id}")
    @RoleRequired({ "ADMIN" })
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deleted successfully", null));
    }

    @GetMapping
    @RoleRequired({ "ADMIN" })
    public ResponseEntity<PaginatedApiResponse<EmployeeResponse>> getEmployees(
            EmployeeFilterRequest filter) {
        Page<EmployeeResponse> employees = employeeService.getEmployeesWithFilters(filter);
        return ResponseEntity.ok(PaginatedApiResponse.of(employees, "Employees retrieved successfully"));
    }

    // Admin endpoint - full filtering capabilities
    @GetMapping("/all")
    @RoleRequired({ "ADMIN" })
    public ResponseEntity<PaginatedApiResponse<EmployeeResponse>> getAllEmployeesForAdmin(
            EmployeeFilterRequest filter) {
        Page<EmployeeResponse> employees = employeeService.getEmployeesWithFilters(filter);
        return ResponseEntity.ok(PaginatedApiResponse.of(employees, "All employees retrieved successfully"));
    }

    // Admin endpoint - view any employee by ID
    @GetMapping("/{id}")
    @RoleRequired({ "ADMIN" })
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeByIdForAdmin(@PathVariable Long id) {
        EmployeeResponse employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success("Employee details retrieved successfully", employee));
    }

    // Manager endpoint - view employees in their department
    @GetMapping("/department")
    @RoleRequired({ "MANAGER" })
    public ResponseEntity<PaginatedApiResponse<EmployeeResponse>> getEmployeesInDepartment(
            EmployeeFilterRequest filter,
            @RequestHeader(value = "X-Employee-Code") String employeeCode) {

        if (employeeCode == null || employeeCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee record has not been created yet");
        }

        Page<EmployeeResponse> employees = employeeService.getEmployeesInManagerDepartment(filter, employeeCode);
        return ResponseEntity.ok(PaginatedApiResponse.of(employees, "Department employees retrieved successfully"));
    }

    // Employee endpoint - view their own details
    @GetMapping("/view")
    @RoleRequired({ "USER" })
    public ResponseEntity<ApiResponse<EmployeeResponse>> getMyDetails(
            @RequestHeader(value = "X-Employee-Code") String employeeCode) {

        if (employeeCode == null || employeeCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee record has not been created yet");
        }

        EmployeeResponse employee = employeeService.getEmployeeByEmployeeCode(employeeCode);
        return ResponseEntity.ok(ApiResponse.success("Employee profile retrieved successfully", employee));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "employee-service");
        status.put("module", "employees");
        return ResponseEntity.ok(ApiResponse.success("Service is healthy", status));
    }

    // Simple test endpoint without auth for gateway testing
    @GetMapping("/public/test")
    public ResponseEntity<ApiResponse<Map<String, String>>> publicTest() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Public test endpoint working");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        response.put("access", "No authentication required");
        return ResponseEntity.ok(ApiResponse.success("Public endpoint test successful", response));
    }

    // Simple test endpoint
    @GetMapping("/admin/test")
    @RoleRequired({ "ADMIN" })
    public ResponseEntity<ApiResponse<Map<String, String>>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Admin test endpoint working");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(ApiResponse.success("Admin endpoint test successful", response));
    }
}