package com.naidugudivada.ecommerce.domain.product.review.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponseDTO(
        UUID id,
        UUID productId,
        UUID customerId,
        String customerName,
        Integer rating,
        String title,
        String comment,
        LocalDateTime createdAt) {
}
