-- Auth Service - Initial schema creation for users table
-- Migration: V1__Create_users_table.sql

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- Insert default admin user (password: admin123)
INSERT INTO users (username, email, password, first_name, last_name, role) 
VALUES (
    'admin', 
    'admin@company.com', 
    '$2a$10$N.oMsaiMZA86Q7dFvdNX5OjRFmT1XALKHWIJQa1J0.J6LiKPdMqQi', 
    'System', 
    'Administrator', 
    'ADMIN'
);

-- Insert default HR manager (password: hr123)
INSERT INTO users (username, email, password, first_name, last_name, role) 
VALUES (
    'hr_manager', 
    'hr@company.com', 
    '$2a$10$9rQ6G5nOF8P9DlhQKUoD6.P7mFNxSgQXaLNM8GhHNR3rS2PZiKJN.',
    'HR', 
    'Manager', 
    'MANAGER'
);

-- Insert default employee (password: emp123)
INSERT INTO users (username, email, password, first_name, last_name, role) 
VALUES (
    'employee1', 
    'emp1@company.com', 
    '$2a$10$8pP5O7pGSgXaNvFNQUoC5.Q6lEMwRfPVYKNL7GgGMQ2qR1OYhJIM.',
    'John', 
    'Employee', 
    'USER'
);