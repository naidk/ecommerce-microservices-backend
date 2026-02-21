package com.naidugudivada.ecommerce.domain.wishlist;

import com.naidugudivada.ecommerce.domain.wishlist.dto.WishlistItemResponseDTO;
import com.naidugudivada.ecommerce.domain.wishlist.dto.WishlistResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WishlistMapper {

    @Mapping(target = "customerId", source = "customer.id")
    WishlistResponseDTO toResponseDTO(WishlistEntity entity);

    WishlistItemResponseDTO toItemResponseDTO(WishlistItemEntity entity);
}
