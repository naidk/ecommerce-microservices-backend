package com.naidugudivada.ecommerce.domain.vendor;

import com.naidugudivada.ecommerce.domain.vendor.dto.VendorRequestDTO;
import com.naidugudivada.ecommerce.domain.vendor.dto.VendorResponseDTO;
import com.naidugudivada.ecommerce.domain.vendor.exceptions.DuplicateVendorException;
import com.naidugudivada.ecommerce.domain.vendor.exceptions.VendorNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.VENDOR_ALREADY_EXISTS_WITH_NAME;
import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.VENDOR_NOT_FOUND_WITH_ID;
import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.VENDOR_TAX_ID_ALREADY_EXISTS;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendorService {

    private final VendorRepository vendorRepository;
    private final VendorMapper vendorMapper;

    @Transactional
    public VendorResponseDTO registerVendor(VendorRequestDTO requestDTO) {
        if (vendorRepository.findByCompanyNameIgnoreCase(requestDTO.companyName()).isPresent()) {
            throw new DuplicateVendorException(
                    String.format(VENDOR_ALREADY_EXISTS_WITH_NAME, requestDTO.companyName()));
        }
        if (vendorRepository.findByTaxId(requestDTO.taxId()).isPresent()) {
            throw new DuplicateVendorException(String.format(VENDOR_TAX_ID_ALREADY_EXISTS, requestDTO.taxId()));
        }

        VendorEntity vendor = vendorMapper.toEntity(requestDTO);
        vendor.setApprovalStatus(VendorApprovalStatus.PENDING); // Default is pending

        VendorEntity savedVendor = vendorRepository.save(vendor);
        log.info("Registered new Vendor: {} with Status: {}", savedVendor.getCompanyName(),
                savedVendor.getApprovalStatus());

        return vendorMapper.toResponseDTO(savedVendor);
    }

    @Transactional
    public VendorResponseDTO approveVendor(UUID id) {
        VendorEntity vendor = getEntity(id);
        vendor.setApprovalStatus(VendorApprovalStatus.APPROVED);
        log.info("Vendor {} has been APPROVED.", vendor.getCompanyName());
        return vendorMapper.toResponseDTO(vendorRepository.save(vendor));
    }

    @Transactional(readOnly = true)
    public Page<VendorResponseDTO> findAll(Pageable pageable) {
        return vendorRepository.findAll(pageable).map(vendorMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public VendorResponseDTO findById(UUID id) {
        return vendorMapper.toResponseDTO(getEntity(id));
    }

    public VendorEntity getEntity(UUID id) {
        return vendorRepository.findById(id)
                .orElseThrow(() -> new VendorNotFoundException(String.format(VENDOR_NOT_FOUND_WITH_ID, id)));
    }
}
