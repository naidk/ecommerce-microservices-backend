package com.naidugudivada.ecommerce.domain.promotion.dto;

import com.naidugudivada.ecommerce.domain.promotion.DiscountTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PromotionRequestDTO(
        @NotBlank String code,
        @NotNull DiscountTypeEnum discountType,
        @NotNull @Positive BigDecimal discountValue,
        @NotNull LocalDateTime startDate,
        @NotNull LocalDateTime endDate,
        boolean isActive) {
}
