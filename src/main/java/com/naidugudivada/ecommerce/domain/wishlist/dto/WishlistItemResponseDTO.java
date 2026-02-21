package com.naidugudivada.ecommerce.domain.wishlist.dto;

import com.naidugudivada.ecommerce.domain.product.dto.ProductResponseDTO;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record WishlistItemResponseDTO(
        UUID id,
        ProductResponseDTO product,
        LocalDateTime addedAt) {
}
