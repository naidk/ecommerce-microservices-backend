package com.naidugudivada.ecommerce.domain.vendor.dto;

import lombok.Builder;
import java.util.UUID;

@Builder
public record VendorResponseDTO(
        UUID id,
        String companyName,
        String taxId,
        String contactEmail,
        String approvalStatus) {
}
