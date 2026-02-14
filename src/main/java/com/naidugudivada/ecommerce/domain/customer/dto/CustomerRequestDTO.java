package com.naidugudivada.ecommerce.domain.customer.dto;

import com.naidugudivada.ecommerce.domain.address.dto.AddressRequestDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

public record CustomerRequestDTO(
                @NotBlank String name,
                @NotBlank @Email String email,
                @NotBlank String phoneNumber,
                @NotBlank String password,
                @NotNull List<AddressRequestDTO> address,
                @DefaultValue("true") Boolean active) {
}
