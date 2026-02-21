package com.naidugudivada.ecommerce.domain.vendor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record VendorRequestDTO(
        @NotBlank(message = "Company Name must not be blank") String companyName,

        @NotBlank(message = "Tax ID must not be blank") String taxId,

        @NotBlank(message = "Contact Email must not be blank") @Email(message = "Contact Email is not valid") String contactEmail) {
}
