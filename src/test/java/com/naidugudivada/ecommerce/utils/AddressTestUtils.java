package com.naidugudivada.ecommerce.utils;

import com.naidugudivada.ecommerce.domain.address.AddressEntity;
import com.naidugudivada.ecommerce.domain.address.AddressTypeEnum;
import com.naidugudivada.ecommerce.domain.address.dto.AddressRequestDTO;
import com.naidugudivada.ecommerce.domain.address.dto.AddressResponseDTO;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

import static com.naidugudivada.ecommerce.utils.TestConstants.ID;

@UtilityClass
public class AddressTestUtils {

    public static AddressEntity createAddressEntity() {
        return AddressEntity.builder()
                .id(ID)
                .street("123 Main St")
                .number("123")
                .neighborhood("Downtown")
                .city("New York")
                .state("NY")
                .country("USA")
                .zipCode("10001")
                .type(AddressTypeEnum.SHIPPING)
                .additionalInfo("Home address")
                .build();
    }

    public static AddressResponseDTO createAddressResponseDTO() {
        var address = createAddressEntity();
        return new AddressResponseDTO(
                address.getId(),
                address.getStreet(),
                address.getNumber(),
                address.getNeighborhood(),
                address.getCity(),
                address.getState(),
                address.getCountry(),
                address.getZipCode(),
                address.getType().name(),
                address.getAdditionalInfo(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public static AddressRequestDTO createAddressRequestDTO() {
        var address = createAddressEntity();
        return new AddressRequestDTO(
                address.getStreet(),
                address.getNumber(),
                address.getNeighborhood(),
                address.getCity(),
                address.getState(),
                address.getCountry(),
                address.getZipCode(),
                address.getType().name(),
                ID,
                address.getAdditionalInfo()
        );
    }
}
