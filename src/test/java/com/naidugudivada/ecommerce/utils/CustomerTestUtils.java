package com.naidugudivada.ecommerce.utils;

import com.naidugudivada.ecommerce.domain.customer.CustomerEntity;
import com.naidugudivada.ecommerce.domain.customer.dto.CustomerPatchDTO;
import com.naidugudivada.ecommerce.domain.customer.dto.CustomerRequestDTO;
import com.naidugudivada.ecommerce.domain.customer.dto.CustomerResponseDTO;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.List;

import static com.naidugudivada.ecommerce.utils.AddressTestUtils.createAddressEntity;
import static com.naidugudivada.ecommerce.utils.AddressTestUtils.createAddressRequestDTO;
import static com.naidugudivada.ecommerce.utils.AddressTestUtils.createAddressResponseDTO;
import static com.naidugudivada.ecommerce.utils.TestConstants.ID;

@UtilityClass
public class CustomerTestUtils {

    public static CustomerEntity createCustomerEntity() {
        return CustomerEntity.builder()
                .id(ID)
                .name("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("1234567890")
                .address(List.of(createAddressEntity()))
                .active(true)
                .build();
    }

    public static CustomerRequestDTO createCustomerRequestDTO() {
        var customer = createCustomerEntity();
        return new CustomerRequestDTO(
                customer.getName(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                "password123",
                List.of(createAddressRequestDTO()),
                customer.getActive());
    }

    public static CustomerResponseDTO createCustomerResponseDTO() {
        var customer = createCustomerEntity();
        return new CustomerResponseDTO(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                List.of(createAddressResponseDTO()),
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    public static CustomerPatchDTO createCustomerPatchDTO() {
        var customer = createCustomerEntity();
        return new CustomerPatchDTO(
                customer.getName(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.getActive());
    }

}
