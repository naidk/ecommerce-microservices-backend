package com.naidugudivada.ecommerce.domain.customer.dto;

import com.naidugudivada.ecommerce.domain.address.dto.AddressResponseDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CustomerResponseDTO(
        UUID id,
        String name,
        String email,
        String phoneNumber,
        List<AddressResponseDTO> address,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
