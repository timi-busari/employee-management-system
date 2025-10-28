package com.employeemgt.employee.service;

import com.employeemgt.employee.dto.DepartmentFilterRequest;
import com.employeemgt.employee.dto.DepartmentRequest;
import com.employeemgt.employee.dto.DepartmentResponse;
import com.employeemgt.employee.entity.Department;
import com.employeemgt.employee.exception.BusinessRuleViolationException;
import com.employeemgt.employee.exception.DuplicateResourceException;
import com.employeemgt.employee.exception.ResourceNotFoundException;
import com.employeemgt.employee.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    public Page<DepartmentResponse> getDepartmentsWithFilters(DepartmentFilterRequest filter) {
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getPerPage());

        // Build specification based on filters
        Specification<Department> spec = buildSpecification(filter);
        
        // Execute query with specification and pagination
        Page<Department> departmentPage = departmentRepository.findAll(spec, pageable);
        
        return departmentPage.map(this::convertToResponse);
    }

    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
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
            throw new BusinessRuleViolationException("Cannot delete department that contains employees. Please reassign employees first.");
        }

        departmentRepository.delete(department);
    }

    /**
     * Build JPA Specification based on department filters
     */
    private Specification<Department> buildSpecification(DepartmentFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Apply filters based on request
            if (filter.hasId()) {
                predicates.add(criteriaBuilder.equal(root.get("id"), filter.getId()));
            }

            if (filter.hasCode()) {
                predicates.add(criteriaBuilder.equal(root.get("code"), filter.getCode()));
            }

            if (filter.hasName()) {
                String namePattern = "%" + filter.getName().trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), namePattern));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
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