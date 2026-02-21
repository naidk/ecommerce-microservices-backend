package com.naidugudivada.ecommerce.domain.vendor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorRepository extends JpaRepository<VendorEntity, UUID> {
    Optional<VendorEntity> findByCompanyNameIgnoreCase(String companyName);

    Optional<VendorEntity> findByTaxId(String taxId);
}
