package com.naidugudivada.ecommerce.domain.shipping.dto;

import com.naidugudivada.ecommerce.domain.shipping.CarrierEnum;
import com.naidugudivada.ecommerce.domain.shipping.ShipmentStatusEnum;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ShipmentResponseDTO(
        UUID id,
        UUID orderId,
        String trackingNumber,
        CarrierEnum carrier,
        ShipmentStatusEnum status,
        LocalDateTime estimatedDeliveryDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
