package com.naidugudivada.ecommerce.domain.vendor;

import com.naidugudivada.ecommerce.domain.vendor.dto.VendorRequestDTO;
import com.naidugudivada.ecommerce.domain.vendor.dto.VendorResponseDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VendorMapper {
    VendorEntity toEntity(VendorRequestDTO dto);

    VendorResponseDTO toResponseDTO(VendorEntity entity);
}
