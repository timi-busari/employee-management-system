package com.employeemgt.employee.service;

import com.employeemgt.employee.dto.DepartmentRequest;
import com.employeemgt.employee.dto.DepartmentResponse;
import com.employeemgt.employee.entity.Department;
import com.employeemgt.employee.exception.DuplicateResourceException;
import com.employeemgt.employee.exception.ResourceNotFoundException;
import com.employeemgt.employee.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return convertToResponse(department);
    }

    public DepartmentResponse getDepartmentByCode(String code) {
        Department department = departmentRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with code: " + code));
        return convertToResponse(department);
    }

    public DepartmentResponse createDepartment(DepartmentRequest request) {
        // Check for duplicate code
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Department with code '" + request.getCode() + "' already exists");
        }
        
        // Check for duplicate name
        if (departmentRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Department with name '" + request.getName() + "' already exists");
        }

        Department department = new Department(
                request.getName(),
                request.getDescription(),
                request.getCode()
        );

        Department savedDepartment = departmentRepository.save(department);
        return convertToResponse(savedDepartment);
    }

    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department existingDepartment = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        // Check for duplicate code (excluding current department)
        if (departmentRepository.existsByCode(request.getCode()) && 
            !existingDepartment.getCode().equals(request.getCode())) {
            throw new DuplicateResourceException("Department with code '" + request.getCode() + "' already exists");
        }
        
        // Check for duplicate name (excluding current department)
        if (departmentRepository.existsByName(request.getName()) && 
            !existingDepartment.getName().equals(request.getName())) {
            throw new DuplicateResourceException("Department with name '" + request.getName() + "' already exists");
        }

        existingDepartment.setName(request.getName());
        existingDepartment.setDescription(request.getDescription());
        existingDepartment.setCode(request.getCode());

        Department updatedDepartment = departmentRepository.save(existingDepartment);
        return convertToResponse(updatedDepartment);
    }

    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        // Check if department has employees
        Long employeeCount = departmentRepository.countEmployeesByDepartmentId(id);
        if (employeeCount > 0) {
            throw new DuplicateResourceException("Cannot delete department with " + employeeCount + " employees. Please reassign employees first.");
        }

        departmentRepository.delete(department);
    }

    public List<DepartmentResponse> searchDepartmentsByName(String name) {
        return departmentRepository.findByNameContaining(name).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Helper method to convert entity to response DTO
    private DepartmentResponse convertToResponse(Department department) {
        Long employeeCount = departmentRepository.countEmployeesByDepartmentId(department.getId());
        
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getDescription(),
                department.getCode(),
                employeeCount,
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }
}