package com.employeemgt.employee.config;

import com.employeemgt.employee.entity.Department;
import com.employeemgt.employee.entity.Employee;
import com.employeemgt.employee.entity.Employee.EmployeeStatus;
import com.employeemgt.employee.repository.DepartmentRepository;
import com.employeemgt.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public void run(String... args) throws Exception {
        // Skip initialization - Flyway migrations will handle sample data
        System.out.println("Skipping data initialization - using Flyway migrations for schema and sample data");
    }

    private void initializeSampleData() {
        // Create departments
        Department itDept = new Department("Information Technology", "IT Department handling software development and infrastructure", "IT");
        Department hrDept = new Department("Human Resources", "HR Department managing employee relations and recruitment", "HR");
        Department financeDept = new Department("Finance", "Finance Department handling accounting and financial operations", "FIN");
        Department marketingDept = new Department("Marketing", "Marketing Department handling promotions and customer outreach", "MKT");

        itDept = departmentRepository.save(itDept);
        hrDept = departmentRepository.save(hrDept);
        financeDept = departmentRepository.save(financeDept);
        marketingDept = departmentRepository.save(marketingDept);

        // Create employees
        Employee emp1 = new Employee("EMP001", "John", "Doe", "john.doe@company.com", 
                                   LocalDate.of(2020, 1, 15), "Software Engineer", 
                                   new BigDecimal("75000.00"), itDept);
        emp1.setPhoneNumber("555-0101");
        emp1.setStatus(EmployeeStatus.ACTIVE);
        emp1.setNotes("Senior developer with expertise in Java and Spring Boot");

        Employee emp2 = new Employee("EMP002", "Jane", "Smith", "jane.smith@company.com", 
                                   LocalDate.of(2019, 3, 20), "HR Manager", 
                                   new BigDecimal("80000.00"), hrDept);
        emp2.setPhoneNumber("555-0102");
        emp2.setStatus(EmployeeStatus.ACTIVE);
        emp2.setNotes("Experienced HR professional with 10+ years in recruitment");

        Employee emp3 = new Employee("EMP003", "Bob", "Johnson", "bob.johnson@company.com", 
                                   LocalDate.of(2021, 6, 10), "Financial Analyst", 
                                   new BigDecimal("65000.00"), financeDept);
        emp3.setPhoneNumber("555-0103");
        emp3.setStatus(EmployeeStatus.ACTIVE);
        emp3.setManagerId(2L); // Jane Smith as manager

        Employee emp4 = new Employee("EMP004", "Alice", "Williams", "alice.williams@company.com", 
                                   LocalDate.of(2022, 2, 1), "Marketing Specialist", 
                                   new BigDecimal("60000.00"), marketingDept);
        emp4.setPhoneNumber("555-0104");
        emp4.setStatus(EmployeeStatus.ACTIVE);

        Employee emp5 = new Employee("EMP005", "Charlie", "Brown", "charlie.brown@company.com", 
                                   LocalDate.of(2018, 9, 15), "Senior Software Engineer", 
                                   new BigDecimal("90000.00"), itDept);
        emp5.setPhoneNumber("555-0105");
        emp5.setStatus(EmployeeStatus.ACTIVE);
        emp5.setNotes("Team lead for backend development");

        employeeRepository.save(emp1);
        employeeRepository.save(emp2);
        employeeRepository.save(emp3);
        employeeRepository.save(emp4);
        employeeRepository.save(emp5);

        System.out.println("Sample data initialized successfully!");
    }
}