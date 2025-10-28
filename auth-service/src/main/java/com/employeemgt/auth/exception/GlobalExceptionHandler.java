package com.employeemgt.auth.exception;

import com.employeemgt.auth.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String SERVICE_NAME = "AUTH-SERVICE";

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.warn("[{}] Authentication failed - RequestId: {}, Path: {}", 
                   SERVICE_NAME, requestId, request.getRequestURI());

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "Authentication Failed",
            "Invalid username or password",
            request.getRequestURI()
        );
        errorResponse.setRequestId(requestId);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(
            UserAlreadyExistsException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.warn("[{}] User already exists - RequestId: {}, Path: {}", 
                   SERVICE_NAME, requestId, request.getRequestURI());

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            "User Already Exists",
            "A user with this username or email already exists",
            request.getRequestURI()
        );
        errorResponse.setRequestId(requestId);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.warn("[{}] Validation error - RequestId: {}, Path: {}", 
                   SERVICE_NAME, requestId, request.getRequestURI());
        
        Map<String, Object> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "Invalid input data provided",
            request.getRequestURI()
        );
        errorResponse.setRequestId(requestId);
        errorResponse.setDetails(Map.of("validation_errors", validationErrors));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.warn("[{}] Method not supported - RequestId: {}, Method: {}, Path: {}", 
                   SERVICE_NAME, requestId, ex.getMethod(), request.getRequestURI());

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.METHOD_NOT_ALLOWED.value(),
            "Method Not Allowed",
            String.format("HTTP method '%s' is not supported for this endpoint", ex.getMethod()),
            request.getRequestURI()
        );
        errorResponse.setRequestId(requestId);

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.warn("[{}] Malformed request body - RequestId: {}, Path: {}", 
                   SERVICE_NAME, requestId, request.getRequestURI());

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Malformed Request",
            "Request body is malformed or contains invalid JSON",
            request.getRequestURI()
        );
        errorResponse.setRequestId(requestId);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.warn("[{}] Unsupported media type - RequestId: {}, MediaType: {}, Path: {}", 
                   SERVICE_NAME, requestId, ex.getContentType(), request.getRequestURI());

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
            "Unsupported Media Type",
            "The media type provided is not supported for this endpoint",
            request.getRequestURI()
        );
        errorResponse.setRequestId(requestId);

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errorResponse);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(
            MissingRequestHeaderException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.warn("[{}] Missing request header - RequestId: {}, Header: {}, Path: {}", 
                   SERVICE_NAME, requestId, ex.getHeaderName(), request.getRequestURI());

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Missing Required Header",
            String.format("Required header '%s' is missing", ex.getHeaderName()),
            request.getRequestURI()
        );
        errorResponse.setRequestId(requestId);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.warn("[{}] Type mismatch - RequestId: {}, Parameter: {}, Path: {}", 
                   SERVICE_NAME, requestId, ex.getName(), request.getRequestURI());

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Parameter",
            String.format("Parameter '%s' has an invalid format", ex.getName()),
            request.getRequestURI()
        );
        errorResponse.setRequestId(requestId);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.warn("[{}] Invalid argument - RequestId: {}, Path: {}", 
                   SERVICE_NAME, requestId, request.getRequestURI());

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Request Data",
            "The provided data is invalid",
            request.getRequestURI()
        );
        errorResponse.setRequestId(requestId);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.warn("[{}] Missing request parameter - RequestId: {}, Parameter: {}, Path: {}", 
                   SERVICE_NAME, requestId, ex.getParameterName(), request.getRequestURI());

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Missing Required Parameter",
            String.format("Required parameter '%s' is missing", ex.getParameterName()),
            request.getRequestURI()
        );
        errorResponse.setRequestId(requestId);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        // Log full exception for debugging (not exposed to client)
        logger.error("[{}] Unexpected error - RequestId: {}, Path: {}", 
                    SERVICE_NAME, requestId, request.getRequestURI(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred. Please try again later",
            request.getRequestURI()
        );
        errorResponse.setRequestId(requestId);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private String generateRequestId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}