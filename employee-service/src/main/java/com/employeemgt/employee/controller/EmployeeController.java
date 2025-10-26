package com.employeemgt.employee.controller;

import com.employeemgt.employee.dto.EmployeeRequest;
import com.employeemgt.employee.dto.EmployeeResponse;
import com.employeemgt.employee.entity.Employee.EmployeeStatus;
import com.employeemgt.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        List<EmployeeResponse> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<EmployeeResponse>> getAllEmployeesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<EmployeeResponse> employees = employeeService.getAllEmployeesPaginated(page, size, sortBy, sortDir);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        EmployeeResponse employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/employee-number/{employeeNumber}")
    public ResponseEntity<EmployeeResponse> getEmployeeByEmployeeNumber(@PathVariable String employeeNumber) {
        EmployeeResponse employee = employeeService.getEmployeeByEmployeeNumber(employeeNumber);
        return ResponseEntity.ok(employee);
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse employee = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable Long id, 
                                                          @Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse employee = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(employee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Employee deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<EmployeeResponse>> getEmployeesByDepartment(@PathVariable Long departmentId) {
        List<EmployeeResponse> employees = employeeService.getEmployeesByDepartment(departmentId);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<EmployeeResponse>> getEmployeesByStatus(@PathVariable EmployeeStatus status) {
        List<EmployeeResponse> employees = employeeService.getEmployeesByStatus(status);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/manager/{managerId}")
    public ResponseEntity<List<EmployeeResponse>> getEmployeesByManager(@PathVariable Long managerId) {
        List<EmployeeResponse> employees = employeeService.getEmployeesByManager(managerId);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/search")
    public ResponseEntity<List<EmployeeResponse>> searchEmployees(@RequestParam String name) {
        List<EmployeeResponse> employees = employeeService.searchEmployeesByName(name);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/hire-date-range")
    public ResponseEntity<List<EmployeeResponse>> getEmployeesByHireDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<EmployeeResponse> employees = employeeService.getEmployeesByHireDateRange(startDate, endDate);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "employee-service");
        status.put("module", "employees");
        return ResponseEntity.ok(status);
    }
}