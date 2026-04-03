package com.ietscroll.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PagedResponseDTO<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean isLast;
    private boolean isFirst;
}