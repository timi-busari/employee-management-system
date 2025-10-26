package com.employeemgt.employee.controller;

import com.employeemgt.employee.dto.DepartmentRequest;
import com.employeemgt.employee.dto.DepartmentResponse;
import com.employeemgt.employee.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "*")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments() {
        List<DepartmentResponse> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getDepartmentById(@PathVariable Long id) {
        DepartmentResponse department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(department);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<DepartmentResponse> getDepartmentByCode(@PathVariable String code) {
        DepartmentResponse department = departmentService.getDepartmentByCode(code);
        return ResponseEntity.ok(department);
    }

    @PostMapping
    public ResponseEntity<DepartmentResponse> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        DepartmentResponse department = departmentService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(department);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponse> updateDepartment(@PathVariable Long id, 
                                                              @Valid @RequestBody DepartmentRequest request) {
        DepartmentResponse department = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(department);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Department deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<DepartmentResponse>> searchDepartments(@RequestParam String name) {
        List<DepartmentResponse> departments = departmentService.searchDepartmentsByName(name);
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "employee-service");
        status.put("module", "departments");
        return ResponseEntity.ok(status);
    }
}