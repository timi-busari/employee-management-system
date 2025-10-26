package com.employeemgt.employee.service;

import com.employeemgt.employee.dto.EmployeeRequest;
import com.employeemgt.employee.dto.EmployeeResponse;
import com.employeemgt.employee.entity.Department;
import com.employeemgt.employee.entity.Employee;
import com.employeemgt.employee.entity.Employee.EmployeeStatus;
import com.employeemgt.employee.exception.DuplicateResourceException;
import com.employeemgt.employee.exception.ResourceNotFoundException;
import com.employeemgt.employee.repository.DepartmentRepository;
import com.employeemgt.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public Page<EmployeeResponse> getAllEmployeesPaginated(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Employee> employeePage = employeeRepository.findAll(pageable);
        
        return employeePage.map(this::convertToResponse);
    }

    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findByIdWithDepartment(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return convertToResponse(employee);
    }

    public EmployeeResponse getEmployeeByEmployeeNumber(String employeeNumber) {
        Employee employee = employeeRepository.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with employee number: " + employeeNumber));
        return convertToResponse(employee);
    }

    public EmployeeResponse createEmployee(EmployeeRequest request) {
        // Check for duplicate employee number
        if (employeeRepository.existsByEmployeeNumber(request.getEmployeeNumber())) {
            throw new DuplicateResourceException("Employee with employee number '" + request.getEmployeeNumber() + "' already exists");
        }
        
        // Check for duplicate email
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Employee with email '" + request.getEmail() + "' already exists");
        }

        // Verify department exists
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        // Verify manager exists if provided
        if (request.getManagerId() != null) {
            if (!employeeRepository.existsById(request.getManagerId())) {
                throw new ResourceNotFoundException("Manager not found with id: " + request.getManagerId());
            }
        }

        Employee employee = new Employee(
                request.getEmployeeNumber(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getHireDate(),
                request.getJobTitle(),
                request.getSalary(),
                department
        );

        employee.setPhoneNumber(request.getPhoneNumber());
        employee.setStatus(request.getStatus());
        employee.setManagerId(request.getManagerId());
        employee.setNotes(request.getNotes());

        Employee savedEmployee = employeeRepository.save(employee);
        return convertToResponse(savedEmployee);
    }

    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        // Check for duplicate employee number (excluding current employee)
        if (employeeRepository.existsByEmployeeNumber(request.getEmployeeNumber()) && 
            !existingEmployee.getEmployeeNumber().equals(request.getEmployeeNumber())) {
            throw new DuplicateResourceException("Employee with employee number '" + request.getEmployeeNumber() + "' already exists");
        }
        
        // Check for duplicate email (excluding current employee)
        if (employeeRepository.existsByEmail(request.getEmail()) && 
            !existingEmployee.getEmail().equals(request.getEmail())) {
            throw new DuplicateResourceException("Employee with email '" + request.getEmail() + "' already exists");
        }

        // Verify department exists
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        // Verify manager exists if provided
        if (request.getManagerId() != null) {
            if (!employeeRepository.existsById(request.getManagerId())) {
                throw new ResourceNotFoundException("Manager not found with id: " + request.getManagerId());
            }
        }

        // Update employee fields
        existingEmployee.setEmployeeNumber(request.getEmployeeNumber());
        existingEmployee.setFirstName(request.getFirstName());
        existingEmployee.setLastName(request.getLastName());
        existingEmployee.setEmail(request.getEmail());
        existingEmployee.setPhoneNumber(request.getPhoneNumber());
        existingEmployee.setHireDate(request.getHireDate());
        existingEmployee.setJobTitle(request.getJobTitle());
        existingEmployee.setSalary(request.getSalary());
        existingEmployee.setStatus(request.getStatus());
        existingEmployee.setDepartment(department);
        existingEmployee.setManagerId(request.getManagerId());
        existingEmployee.setNotes(request.getNotes());

        Employee updatedEmployee = employeeRepository.save(existingEmployee);
        return convertToResponse(updatedEmployee);
    }

    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        employeeRepository.delete(employee);
    }

    public List<EmployeeResponse> getEmployeesByDepartment(Long departmentId) {
        return employeeRepository.findByDepartmentId(departmentId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<EmployeeResponse> getEmployeesByStatus(EmployeeStatus status) {
        return employeeRepository.findByStatus(status).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<EmployeeResponse> getEmployeesByManager(Long managerId) {
        return employeeRepository.findByManagerId(managerId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<EmployeeResponse> searchEmployeesByName(String name) {
        return employeeRepository.findByNameContaining(name).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<EmployeeResponse> getEmployeesByHireDateRange(LocalDate startDate, LocalDate endDate) {
        return employeeRepository.findByHireDateBetween(startDate, endDate).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Helper method to convert entity to response DTO
    private EmployeeResponse convertToResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setEmployeeNumber(employee.getEmployeeNumber());
        response.setFirstName(employee.getFirstName());
        response.setLastName(employee.getLastName());
        response.setFullName(employee.getFullName());
        response.setEmail(employee.getEmail());
        response.setPhoneNumber(employee.getPhoneNumber());
        response.setHireDate(employee.getHireDate());
        response.setJobTitle(employee.getJobTitle());
        response.setSalary(employee.getSalary());
        response.setStatus(employee.getStatus());
        response.setManagerId(employee.getManagerId());
        response.setNotes(employee.getNotes());
        response.setCreatedAt(employee.getCreatedAt());
        response.setUpdatedAt(employee.getUpdatedAt());

        // Set department summary
        if (employee.getDepartment() != null) {
            EmployeeResponse.DepartmentSummary deptSummary = new EmployeeResponse.DepartmentSummary(
                    employee.getDepartment().getId(),
                    employee.getDepartment().getName(),
                    employee.getDepartment().getCode()
            );
            response.setDepartment(deptSummary);
        }

        return response;
    }
}