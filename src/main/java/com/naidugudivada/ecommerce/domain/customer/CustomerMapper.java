package com.naidugudivada.ecommerce.domain.customer;

import com.naidugudivada.ecommerce.domain.address.AddressMapper;
import com.naidugudivada.ecommerce.domain.customer.dto.CustomerPatchDTO;
import com.naidugudivada.ecommerce.domain.customer.dto.CustomerRequestDTO;
import com.naidugudivada.ecommerce.domain.customer.dto.CustomerResponseDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses = AddressMapper.class)
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", defaultValue = "true")
    CustomerEntity toEntity(CustomerRequestDTO customerRequestDTO);

    CustomerResponseDTO toResponseDTO(CustomerEntity customerEntity);

    default CustomerEntity toEntityWithAddresses(CustomerRequestDTO customerRequestDTO) {
        CustomerEntity customerEntity = toEntity(customerRequestDTO);

        if (customerEntity.getAddress() != null) {
            customerEntity.getAddress().forEach(address -> address.setCustomer(customerEntity));
        }

        return customerEntity;
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patchCustomerFromDto(CustomerPatchDTO dto, @MappingTarget CustomerEntity entity);

}
