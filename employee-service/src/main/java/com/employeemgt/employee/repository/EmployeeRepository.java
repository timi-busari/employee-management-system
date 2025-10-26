package com.employeemgt.employee.repository;

import com.employeemgt.employee.entity.Employee;
import com.employeemgt.employee.entity.Employee.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeNumber(String employeeNumber);

    Optional<Employee> findByEmail(String email);

    boolean existsByEmployeeNumber(String employeeNumber);

    boolean existsByEmail(String email);

    List<Employee> findByStatus(EmployeeStatus status);

    List<Employee> findByDepartmentId(Long departmentId);

    Page<Employee> findByDepartmentId(Long departmentId, Pageable pageable);

    List<Employee> findByManagerId(Long managerId);

    @Query("SELECT e FROM Employee e WHERE e.firstName LIKE %:name% OR e.lastName LIKE %:name%")
    List<Employee> findByNameContaining(@Param("name") String name);

    @Query("SELECT e FROM Employee e WHERE e.hireDate BETWEEN :startDate AND :endDate")
    List<Employee> findByHireDateBetween(@Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);

    @Query("SELECT e FROM Employee e JOIN FETCH e.department WHERE e.id = :id")
    Optional<Employee> findByIdWithDepartment(@Param("id") Long id);

    @Query("SELECT e FROM Employee e WHERE e.jobTitle LIKE %:jobTitle%")
    List<Employee> findByJobTitleContaining(@Param("jobTitle") String jobTitle);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.status = :status")
    Long countByStatus(@Param("status") EmployeeStatus status);

    @Query("SELECT e FROM Employee e WHERE e.status = :status AND e.department.id = :departmentId")
    List<Employee> findByStatusAndDepartmentId(@Param("status") EmployeeStatus status, 
                                              @Param("departmentId") Long departmentId);
}