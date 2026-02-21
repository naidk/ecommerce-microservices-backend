package com.naidugudivada.ecommerce.domain.vendor;

import com.naidugudivada.ecommerce.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity(name = "Vendor")
@Table(name = "vendor")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class VendorEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String companyName;

    @Column(nullable = false, unique = true)
    private String taxId;

    @Column(nullable = false)
    private String contactEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VendorApprovalStatus approvalStatus;
}
