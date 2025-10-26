package com.employeemgt.employee.repository;

import com.employeemgt.employee.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByCode(String code);

    Optional<Department> findByName(String name);

    boolean existsByCode(String code);

    boolean existsByName(String name);

    @Query("SELECT d FROM Department d WHERE d.name LIKE %:name%")
    List<Department> findByNameContaining(@Param("name") String name);

    @Query("SELECT d FROM Department d JOIN FETCH d.employees WHERE d.id = :id")
    Optional<Department> findByIdWithEmployees(@Param("id") Long id);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :departmentId")
    Long countEmployeesByDepartmentId(@Param("departmentId") Long departmentId);
}