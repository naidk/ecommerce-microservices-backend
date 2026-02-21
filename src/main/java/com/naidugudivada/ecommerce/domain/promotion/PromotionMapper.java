package com.naidugudivada.ecommerce.domain.promotion;

import com.naidugudivada.ecommerce.domain.promotion.dto.PromotionRequestDTO;
import com.naidugudivada.ecommerce.domain.promotion.dto.PromotionResponseDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PromotionMapper {
    PromotionEntity toEntity(PromotionRequestDTO request);

    PromotionResponseDTO toResponseDTO(PromotionEntity entity);
}
