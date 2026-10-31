package org.example.order.web.request;

import org.springframework.data.domain.Sort;

public record DefaultPageRequest(int page, int size, SortField sortBy, Sort.Direction direction) {

    public static DefaultPageRequest fromQueryParams(
            Integer page,
            Integer size,
            SortField sortBy,
            Sort.Direction direction
    ) {
        return new DefaultPageRequest(
                page == null ? 0 : page,
                size == null ? 20 : size,
                sortBy == null ? SortField.CREATED_AT : sortBy,
                direction == null ? Sort.Direction.DESC : direction
        );
    }
}
