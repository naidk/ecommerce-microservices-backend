package com.naidugudivada.ecommerce.domain.product;

import com.naidugudivada.ecommerce.domain.product.dto.ProductRequestDTO;
import com.naidugudivada.ecommerce.domain.product.dto.ProductResponseDTO;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", defaultValue = "true")
    @Mapping(target = "vendor.id", source = "vendorId")
    @Mapping(target = "averageRating", constant = "0.0")
    @Mapping(target = "totalReviews", constant = "0")
    ProductEntity toEntity(ProductRequestDTO dto);

    @Mapping(target = "vendorId", source = "vendor.id")
    ProductResponseDTO toResponseDTO(ProductEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vendor.id", source = "vendorId")
    void updateEntityFromDto(ProductRequestDTO dto, @MappingTarget ProductEntity entity);
}
