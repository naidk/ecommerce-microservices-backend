package com.naidugudivada.ecommerce.domain.order.dto;

import com.naidugudivada.ecommerce.domain.customer.dto.CustomerResponseDTO;
import com.naidugudivada.ecommerce.domain.order.orderaddress.dto.OrderAddressResponseDTO;
import com.naidugudivada.ecommerce.domain.order.orderitem.dto.OrderItemResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponseDTO(
    UUID id,
    CustomerResponseDTO customer,
    List<OrderItemResponseDTO> orderItems,
    OrderAddressResponseDTO shippingAddress,
    BigDecimal totalPrice,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
