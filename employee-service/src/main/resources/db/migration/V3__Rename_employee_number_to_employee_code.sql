-- Migration to rename employee_number column to employee_code
-- Migration: V3__Rename_employee_number_to_employee_code.sql

-- Rename the column from employee_number to employee_code
ALTER TABLE employees RENAME COLUMN employee_number TO employee_code;

-- Drop the old index
DROP INDEX IF EXISTS idx_employees_employee_number;

-- Create new index with the updated column name
CREATE INDEX idx_employees_employee_code ON employees(employee_code);

-- Add comment to document the change
COMMENT ON COLUMN employees.employee_code IS 'Unique employee identifier, renamed from employee_number for consistency with auth service';