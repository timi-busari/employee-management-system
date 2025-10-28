package com.employeemgt.employee.service;

import com.employeemgt.employee.dto.EmployeeFilterRequest;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeEventPublisher eventPublisher;

    public EmployeeResponse createEmployee(EmployeeRequest request) {
        // Check for duplicate employee number
        if (employeeRepository.existsByEmployeeNumber(request.getEmployeeNumber())) {
            throw new DuplicateResourceException(
                    "Employee with employee number '" + request.getEmployeeNumber() + "' already exists");
        }

        // Check for duplicate email
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Employee with email '" + request.getEmail() + "' already exists");
        }

        // Verify department exists
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + request.getDepartmentId()));

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
                department);

        employee.setPhoneNumber(request.getPhoneNumber());
        employee.setStatus(request.getStatus());
        employee.setManagerId(request.getManagerId());
        employee.setNotes(request.getNotes());

        Employee savedEmployee = employeeRepository.save(employee);

        // Publish employee created event
        eventPublisher.publishEmployeeCreated(
                savedEmployee.getId(),
                savedEmployee.getFirstName(),
                savedEmployee.getLastName(),
                savedEmployee.getEmail(),
                savedEmployee.getDepartment().getName(),
                "system" // TODO: Get actual user from security context
        );

        return convertToResponse(savedEmployee);
    }

    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        // Check for duplicate employee number (excluding current employee)
        if (employeeRepository.existsByEmployeeNumber(request.getEmployeeNumber()) &&
                !existingEmployee.getEmployeeNumber().equals(request.getEmployeeNumber())) {
            throw new DuplicateResourceException(
                    "Employee with employee number '" + request.getEmployeeNumber() + "' already exists");
        }

        // Check for duplicate email (excluding current employee)
        if (employeeRepository.existsByEmail(request.getEmail()) &&
                !existingEmployee.getEmail().equals(request.getEmail())) {
            throw new DuplicateResourceException("Employee with email '" + request.getEmail() + "' already exists");
        }

        // Verify department exists
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + request.getDepartmentId()));

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

        // Publish employee updated event
        eventPublisher.publishEmployeeUpdated(
                updatedEmployee.getId(),
                updatedEmployee.getFirstName(),
                updatedEmployee.getLastName(),
                updatedEmployee.getEmail(),
                updatedEmployee.getDepartment().getName(),
                "system" // TODO: Get actual user from security context
        );

        return convertToResponse(updatedEmployee);
    }

    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        // Publish employee deleted event before deletion
        eventPublisher.publishEmployeeDeleted(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getDepartment() != null ? employee.getDepartment().getName() : "Unknown",
                "system" // TODO: Get actual user from security context
        );

        employeeRepository.delete(employee);
    }

    /**
     * Get employees with filters - role-based access control handled at controller level
     * 
     * @param filterRequest The filter criteria
     * @return Page of employees based on filters
     */
    public Page<EmployeeResponse> getEmployeesWithFilters(EmployeeFilterRequest filterRequest) {
        Pageable pageable = PageRequest.of(filterRequest.getPage(), filterRequest.getPerPage());

        // Build specification based on filters only
        Specification<Employee> spec = buildSpecification(filterRequest);

        Page<Employee> employeePage = employeeRepository.findAll(spec, pageable);
        return employeePage.map(this::convertToResponse);
    }

    /**
     * Get employee by ID (access control handled at controller level)
     * 
     * @param id The employee ID
     * @return Employee details
     */
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findByIdWithDepartment(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        return convertToResponse(employee);
    }

    /**
     * Build JPA Specification based on filters only
     * Role-based access control is handled at the controller level
     */
    private Specification<Employee> buildSpecification(EmployeeFilterRequest filterRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Apply filters based on request
            if (filterRequest.getDepartmentId() != null) {
                predicates
                        .add(criteriaBuilder.equal(root.get("department").get("id"), filterRequest.getDepartmentId()));
            }

            if (filterRequest.getStatus() != null && !filterRequest.getStatus().trim().isEmpty()) {
                try {
                    EmployeeStatus status = EmployeeStatus.valueOf(filterRequest.getStatus().toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("status"), status));
                } catch (IllegalArgumentException e) {
                    // Invalid status value - ignore filter
                }
            }

            if (filterRequest.getManagerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("managerId"), filterRequest.getManagerId()));
            }

            if (filterRequest.getName() != null && !filterRequest.getName().trim().isEmpty()) {
                String namePattern = "%" + filterRequest.getName().trim().toLowerCase() + "%";
                Predicate firstNameMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("firstName")), namePattern);
                Predicate lastNameMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("lastName")), namePattern);
                predicates.add(criteriaBuilder.or(firstNameMatch, lastNameMatch));
            }

            if (filterRequest.getHireDateFrom() != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("hireDate"), filterRequest.getHireDateFrom()));
            }

            if (filterRequest.getHireDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("hireDate"), filterRequest.getHireDateTo()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
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
                    employee.getDepartment().getCode());
            response.setDepartment(deptSummary);
        }

        return response;
    }
}