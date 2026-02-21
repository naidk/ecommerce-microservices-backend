package com.naidugudivada.ecommerce.domain.shipping;

import com.naidugudivada.ecommerce.domain.shipping.dto.ShipmentResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {

    @Mapping(target = "orderId", source = "order.id")
    ShipmentResponseDTO toResponseDTO(ShipmentEntity entity);
}
