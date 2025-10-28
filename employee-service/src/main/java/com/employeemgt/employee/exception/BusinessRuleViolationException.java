package com.employeemgt.employee.exception;

/**
 * Exception thrown when a business rule is violated
 * This should return HTTP 422 Unprocessable Entity
 */
public class BusinessRuleViolationException extends RuntimeException {
    
    public BusinessRuleViolationException(String message) {
        super(message);
    }
    
    public BusinessRuleViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}