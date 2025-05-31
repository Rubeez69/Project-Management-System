package com.project_management.final_project.util;

import com.project_management.final_project.dto.response.PageResponse;
import org.springframework.data.domain.Page;

public class PaginationUtil {
    
    /**
     * Create a PageResponse from a Spring Page object
     * @param page The Spring Page object
     * @return PageResponse object
     * @param <T> The type of content in the page
     */
    public static <T> PageResponse<T> createPageResponse(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
} 