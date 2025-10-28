-- Employee Service - Create employees table
-- Migration: V2__Create_employees_table.sql

CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    employee_number VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(15),
    hire_date DATE NOT NULL,
    job_title VARCHAR(100) NOT NULL,
    salary DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    department_id BIGINT NOT NULL REFERENCES departments(id),
    manager_id BIGINT REFERENCES employees(id),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_employees_employee_number ON employees(employee_number);
CREATE INDEX idx_employees_email ON employees(email);
CREATE INDEX idx_employees_department_id ON employees(department_id);
CREATE INDEX idx_employees_manager_id ON employees(manager_id);
CREATE INDEX idx_employees_status ON employees(status);
CREATE INDEX idx_employees_hire_date ON employees(hire_date);

-- Insert sample employees
INSERT INTO employees (employee_number, first_name, last_name, email, phone_number, hire_date, job_title, salary, status, department_id, notes) VALUES 
('EMP001', 'John', 'Doe', 'john.doe@company.com', '555-0101', '2020-01-15', 'Senior Software Engineer', 75000.00, 'ACTIVE', 1, 'Senior developer with expertise in Java and Spring Boot'),
('EMP002', 'Jane', 'Smith', 'jane.smith@company.com', '555-0102', '2019-03-20', 'HR Manager', 80000.00, 'ACTIVE', 2, 'Experienced HR professional with 10+ years in recruitment'),
('EMP003', 'Bob', 'Johnson', 'bob.johnson@company.com', '555-0103', '2021-06-10', 'Financial Analyst', 65000.00, 'ACTIVE', 3, 'CPA with expertise in financial modeling'),
('EMP004', 'Alice', 'Williams', 'alice.williams@company.com', '555-0104', '2022-02-01', 'Marketing Specialist', 60000.00, 'ACTIVE', 4, 'Digital marketing expert with social media focus'),
('EMP005', 'Charlie', 'Brown', 'charlie.brown@company.com', '555-0105', '2018-09-15', 'DevOps Engineer', 90000.00, 'ACTIVE', 1, 'Team lead for backend development and infrastructure');

-- Update manager relationships
UPDATE employees SET manager_id = 2 WHERE id IN (3); -- Bob reports to Jane (HR Manager)
UPDATE employees SET manager_id = 1 WHERE id IN (5); -- Charlie reports to John (Senior SE)