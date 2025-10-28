package com.employeemgt.employee.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Paginated response wrapper for collection endpoints
 * Pagination information is included in the meta field
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaginatedApiResponse<T> extends ApiResponse<List<T>> {
    
    public PaginatedApiResponse() {
        super();
    }
    
    public PaginatedApiResponse(Page<T> page) {
        super(true, "Data retrieved successfully", page.getContent());
        
        // Set pagination information directly in meta field
        this.setMeta(createPaginationInfo(page));
    }
    
    public static <T> PaginatedApiResponse<T> of(Page<T> page) {
        return new PaginatedApiResponse<>(page);
    }
    
    public static <T> PaginatedApiResponse<T> of(Page<T> page, String message) {
        PaginatedApiResponse<T> response = new PaginatedApiResponse<>(page);
        response.setMessage(message);
        return response;
    }
    
    private Map<String, Object> createPaginationInfo(Page<?> page) {
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page.getNumber() + 1); // 1-based for user display
        pagination.put("perPage", page.getSize());
        pagination.put("totalPages", page.getTotalPages());
        pagination.put("total", page.getTotalElements());
        pagination.put("hasNext", page.hasNext());
        pagination.put("hasPrevious", page.hasPrevious());
        pagination.put("isFirst", page.isFirst());
        pagination.put("isLast", page.isLast());
        return pagination;
    }
}