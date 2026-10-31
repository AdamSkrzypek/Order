package org.example.order.web.request;

import org.example.order.domain.entity.AbstractEntity;

public enum SortField {
    CREATED_AT(AbstractEntity.Fields.createdAt),
    UPDATED_AT(AbstractEntity.Fields.updatedAt);

    private final String property;

    SortField(String property) {
        this.property = property;
    }

    public String property() {
        return property;
    }
}