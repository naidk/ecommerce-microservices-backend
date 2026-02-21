package com.naidugudivada.ecommerce.domain.promotion.dto;

import com.naidugudivada.ecommerce.domain.promotion.DiscountTypeEnum;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PromotionResponseDTO(
        UUID id,
        String code,
        DiscountTypeEnum discountType,
        BigDecimal discountValue,
        LocalDateTime startDate,
        LocalDateTime endDate,
        boolean isActive) {
}
