package com.emmano.inventory_api.product.dto;

import jakarta.persistence.criteria.CriteriaBuilder;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String name,String sku,
        String description,
        BigDecimal price,
        Long categoryId,
        String categoryName,
        Long supplierId,
        String supplierName,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
