package com.employeemgt.employee.repository;

import com.employeemgt.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    boolean existsByEmployeeCode(String employeeCode);

    Optional<Employee> findByEmployeeCode(String employeeCode);

    boolean existsByEmail(String email);
    
    // Email based lookups for alternative auth scenarios
    Optional<Employee> findByEmail(String email);

    @Query("SELECT e FROM Employee e JOIN FETCH e.department WHERE e.id = :id")
    Optional<Employee> findByIdWithDepartment(@Param("id") Long id);
}