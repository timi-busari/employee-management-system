-- Employee Service - Initial schema creation
-- Migration: V1__Create_departments_table.sql

CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(500),
    code VARCHAR(10) UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_departments_name ON departments(name);
CREATE INDEX idx_departments_code ON departments(code);

-- Insert sample departments
INSERT INTO departments (name, description, code) VALUES 
('Information Technology', 'IT Department handling software development and infrastructure', 'IT'),
('Human Resources', 'HR Department managing employee relations and recruitment', 'HR'),
('Finance', 'Finance Department handling accounting and financial operations', 'FIN'),
('Marketing', 'Marketing Department handling promotions and customer outreach', 'MKT'),
('Operations', 'Operations Department managing day-to-day business operations', 'OPS');