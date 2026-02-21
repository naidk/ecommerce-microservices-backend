package com.naidugudivada.ecommerce.domain.product.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReviewRequestDTO(
        @NotNull(message = "Product ID is required") UUID productId,

        @NotNull(message = "Rating is required") @Min(value = 1, message = "Rating must be between 1 and 5") @Max(value = 5, message = "Rating must be between 1 and 5") Integer rating,

        @NotBlank(message = "Title is required") String title,

        @NotBlank(message = "Comment is required") String comment) {
}
