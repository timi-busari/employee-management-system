package com.employeemgt.employee.service;

import com.employeemgt.employee.dto.DepartmentFilterRequest;
import com.employeemgt.employee.dto.DepartmentRequest;
import com.employeemgt.employee.dto.DepartmentResponse;
import com.employeemgt.employee.entity.Department;
import com.employeemgt.employee.exception.BusinessRuleViolationException;
import com.employeemgt.employee.exception.DuplicateResourceException;
import com.employeemgt.employee.exception.ResourceNotFoundException;
import com.employeemgt.employee.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private DepartmentRequest departmentRequest;
    private Department department;
    private DepartmentResponse departmentResponse;

    @BeforeEach
    void setUp() {
        departmentRequest = new DepartmentRequest();
        departmentRequest.setName("Engineering");
        departmentRequest.setCode("ENG");
        departmentRequest.setDescription("Engineering Department");

        department = new Department();
        department.setId(1L);
        department.setName("Engineering");
        department.setCode("ENG");
        department.setDescription("Engineering Department");
        department.setCreatedAt(LocalDateTime.now());
        department.setUpdatedAt(LocalDateTime.now());

        departmentResponse = new DepartmentResponse();
        departmentResponse.setId(1L);
        departmentResponse.setName("Engineering");
        departmentResponse.setCode("ENG");
        departmentResponse.setDescription("Engineering Department");
        departmentResponse.setEmployeeCount(0L);
        departmentResponse.setCreatedAt(LocalDateTime.now());
        departmentResponse.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createDepartment_WithValidData_ShouldCreateDepartment() {
        // Arrange
        when(departmentRepository.existsByName(departmentRequest.getName())).thenReturn(false);
        when(departmentRepository.existsByCode(departmentRequest.getCode())).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        // Act
        DepartmentResponse response = departmentService.createDepartment(departmentRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Engineering", response.getName());
        assertEquals("ENG", response.getCode());
        assertEquals("Engineering Department", response.getDescription());
        
        verify(departmentRepository).existsByName(departmentRequest.getName());
        verify(departmentRepository).existsByCode(departmentRequest.getCode());
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void createDepartment_WithDuplicateName_ShouldThrowDuplicateResourceException() {
        // Arrange
        when(departmentRepository.existsByName(departmentRequest.getName())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            departmentService.createDepartment(departmentRequest);
        });

        verify(departmentRepository).existsByName(departmentRequest.getName());
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void createDepartment_WithDuplicateCode_ShouldThrowDuplicateResourceException() {
        // Arrange
        when(departmentRepository.existsByCode(departmentRequest.getCode())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            departmentService.createDepartment(departmentRequest);
        });

        verify(departmentRepository).existsByCode(departmentRequest.getCode());
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void updateDepartment_WithValidData_ShouldUpdateDepartment() {
        // Arrange
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByName(departmentRequest.getName())).thenReturn(false);
        when(departmentRepository.existsByCode(departmentRequest.getCode())).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        // Act
        DepartmentResponse response = departmentService.updateDepartment(1L, departmentRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Engineering", response.getName());
        assertEquals("ENG", response.getCode());
        
        verify(departmentRepository).findById(1L);
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void updateDepartment_WithInvalidId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            departmentService.updateDepartment(999L, departmentRequest);
        });

        verify(departmentRepository).findById(999L);
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void getDepartmentById_WithValidId_ShouldReturnDepartment() {
        // Arrange
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        // Act
        DepartmentResponse response = departmentService.getDepartmentById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Engineering", response.getName());
        assertEquals("ENG", response.getCode());

        verify(departmentRepository).findById(1L);
    }

    @Test
    void getDepartmentById_WithInvalidId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            departmentService.getDepartmentById(999L);
        });

        verify(departmentRepository).findById(999L);
    }

    @Test
    void getDepartmentsWithFilters_ShouldReturnPagedDepartments() {
        // Arrange
        List<Department> departments = Arrays.asList(department);
        Page<Department> departmentPage = new PageImpl<>(departments);
        DepartmentFilterRequest filter = new DepartmentFilterRequest();
        filter.setPage(0);
        filter.setPerPage(10);
        
        when(departmentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(departmentPage);

        // Act
        Page<DepartmentResponse> response = departmentService.getDepartmentsWithFilters(filter);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("Engineering", response.getContent().get(0).getName());

        verify(departmentRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void deleteDepartment_WithValidId_ShouldDeleteDepartment() {
        // Arrange
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.countEmployeesByDepartmentId(1L)).thenReturn(0L);
        doNothing().when(departmentRepository).delete(department);

        // Act
        departmentService.deleteDepartment(1L);

        // Assert
        verify(departmentRepository).findById(1L);
        verify(departmentRepository).countEmployeesByDepartmentId(1L);
        verify(departmentRepository).delete(department);
    }

    @Test
    void deleteDepartment_WithInvalidId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            departmentService.deleteDepartment(999L);
        });

        verify(departmentRepository).findById(999L);
        verify(departmentRepository, never()).delete(any(Department.class));
    }

    @Test
    void deleteDepartment_WithEmployees_ShouldThrowBusinessRuleViolationException() {
        // Arrange
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.countEmployeesByDepartmentId(1L)).thenReturn(5L);

        // Act & Assert
        assertThrows(BusinessRuleViolationException.class, () -> {
            departmentService.deleteDepartment(1L);
        });

        verify(departmentRepository).findById(1L);
        verify(departmentRepository).countEmployeesByDepartmentId(1L);
        verify(departmentRepository, never()).delete(any(Department.class));
    }
}