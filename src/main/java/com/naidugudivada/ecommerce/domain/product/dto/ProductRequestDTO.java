package com.naidugudivada.ecommerce.domain.product.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProductRequestDTO(@NotBlank
                                String sku,
                                @NotBlank
                                String name,
                                @NotBlank
                                String label,
                                @NotNull
                                String category,
                                @NotNull
                                @Positive
                                BigDecimal price,
                                @NotNull
                                @DecimalMin("0.0")
                                @DecimalMax("100.0")
                                BigDecimal discount,
                                @NotNull
                                @Positive
                                @Max(20)
                                Integer installments,
                                @PositiveOrZero
                                Integer stockQuantity,
                                Boolean active
) {
}
