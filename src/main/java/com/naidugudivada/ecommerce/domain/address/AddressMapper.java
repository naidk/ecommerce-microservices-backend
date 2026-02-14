package com.naidugudivada.ecommerce.domain.address;

import com.naidugudivada.ecommerce.domain.address.dto.AddressRequestDTO;
import com.naidugudivada.ecommerce.domain.address.dto.AddressResponseDTO;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface AddressMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    AddressEntity toEntity(AddressRequestDTO dto);

    AddressResponseDTO toResponseDTO(AddressEntity entity);
}
