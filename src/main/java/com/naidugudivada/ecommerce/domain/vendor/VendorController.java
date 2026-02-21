package com.naidugudivada.ecommerce.domain.vendor;

import com.naidugudivada.ecommerce.domain.vendor.dto.VendorRequestDTO;
import com.naidugudivada.ecommerce.domain.vendor.dto.VendorResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vendor")
public class VendorController {

    private final VendorService vendorService;

    @PostMapping("/register")
    public ResponseEntity<VendorResponseDTO> registerVendor(@Valid @RequestBody VendorRequestDTO vendorRequest) {
        VendorResponseDTO response = vendorService.registerVendor(vendorRequest);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<VendorResponseDTO> approveVendor(@PathVariable UUID id) {
        // In a real application, this should clearly be restricted to an ADMIN role via
        // method security
        return ResponseEntity.ok(vendorService.approveVendor(id));
    }

    @GetMapping
    public ResponseEntity<Page<VendorResponseDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(vendorService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(vendorService.findById(id));
    }
}
