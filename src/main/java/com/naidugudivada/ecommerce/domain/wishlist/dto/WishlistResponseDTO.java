package com.naidugudivada.ecommerce.domain.wishlist.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record WishlistResponseDTO(
        UUID id,
        UUID customerId,
        List<WishlistItemResponseDTO> items) {
}
