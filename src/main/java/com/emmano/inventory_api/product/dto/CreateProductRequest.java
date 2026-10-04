package com.emmano.inventory_api.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank(message = "Product name is required")
        @Size(max = 200,message = "Product name cannot exceed 200 char")
        String name,
        @NotBlank(message = "SKU is required")
        @Size(max = 64,message = "sku cannot exceed 64 chars")
        String sku,
        @Size( max = 1000,message = "Description cannot exceed 1000 characters")
        String description,
        @NotNull(message = "price is required")
        @Positive(message = "Price must be greater than zero")
        @Digits(integer = 17,fraction = 2,message = "Price can have at most 17 integer digits and 2 decimal places")
        BigDecimal price,
        @NotNull(message = "Category ID is required")
        @Positive(message = "CategoryId must be positive")
        Long categoryId,
        @NotNull(message = "SupplierId is required")
        @Positive(message = "Supplier ID must be positive")
        Long supplierId


) {
}
