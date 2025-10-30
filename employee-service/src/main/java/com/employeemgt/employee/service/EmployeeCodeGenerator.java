package com.employeemgt.employee.service;

import com.employeemgt.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class EmployeeCodeGenerator {

    @Autowired
    private EmployeeRepository employeeRepository;

    private static final String EMPLOYEE_PREFIX = "EMP";
    private static final String MANAGER_PREFIX = "MGR";
    private static final String ADMIN_PREFIX = "ADM";

    /**
     * Generate unique employee code based on role and current timestamp
     * Format: {PREFIX}{YYYYMM}{RANDOM_3_DIGITS}
     * Example: EMP202410001, MGR202410002
     */
    public String generateEmployeeCode(String role) {
        String prefix = determinePrefix(role);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));

        String employeeCode;
        int attempts = 0;
        int maxAttempts = 100;

        do {
            String randomSuffix = String.format("%03d", new Random().nextInt(1000));
            employeeCode = prefix + timestamp + randomSuffix;
            attempts++;

            if (attempts > maxAttempts) {
                throw new RuntimeException(
                        "Unable to generate unique employee code after " + maxAttempts + " attempts");
            }
        } while (employeeRepository.existsByEmployeeCode(employeeCode));

        return employeeCode;
    }

    private String determinePrefix(String role) {
        if (role == null) {
            return EMPLOYEE_PREFIX;
        }

        switch (role.toUpperCase()) {
            case "ADMIN":
                return ADMIN_PREFIX;
            case "MANAGER":
                return MANAGER_PREFIX;
            case "USER":
            case "EMPLOYEE":
            default:
                return EMPLOYEE_PREFIX;
        }
    }
}