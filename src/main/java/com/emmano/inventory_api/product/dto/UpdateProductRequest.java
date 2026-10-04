package com.emmano.inventory_api.product.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProductRequest(
        @Pattern(
                regexp = ".*\\S.*",
                message = "Product name cannot be blank"
        )
        @Size(
                max = 200,
                message = "Product name cannot exceed 200 characters"
        )
        String name,

        @Size(
                max = 1000,
                message = "Description cannot exceed 1000 characters"
        )
        String description,

        @Positive(message = "Price must be greater than zero")
        @Digits(
                integer = 17,
                fraction = 2,
                message = "Price can have at most 17 integer digits and 2 decimal places"
        )
        BigDecimal price,

        @Positive(message = "Category ID must be positive")
        Long categoryId,

        @Positive(message = "Supplier ID must be positive")
        Long supplierId
) {
    @AssertTrue(message = "At least one product field must be provided")
    public boolean isAnyFieldPresent() {
        return name != null
                || description != null
                || price != null
                || categoryId != null
                || supplierId != null;
    }
}