package com.naidugudivada.ecommerce.domain.wishlist;

import com.naidugudivada.ecommerce.domain.customer.CustomerEntity;
import com.naidugudivada.ecommerce.domain.customer.CustomerRepository;
import com.naidugudivada.ecommerce.domain.wishlist.dto.WishlistResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.UUID;

import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.CUSTOMER_NOT_FOUND_WITH_ID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final CustomerRepository customerRepository;

    @PostMapping("/{productId}")
    public ResponseEntity<WishlistResponseDTO> addProductToWishlist(@PathVariable UUID productId, Principal principal) {
        UUID customerId = getCustomerIdFromPrincipal(principal);
        return ResponseEntity.ok(wishlistService.addProductToWishlist(customerId, productId));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeProductFromWishlist(@PathVariable UUID productId, Principal principal) {
        UUID customerId = getCustomerIdFromPrincipal(principal);
        wishlistService.removeProductFromWishlist(customerId, productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<WishlistResponseDTO> getMyWishlist(Principal principal) {
        UUID customerId = getCustomerIdFromPrincipal(principal);
        return ResponseEntity.ok(wishlistService.getWishlist(customerId));
    }

    private UUID getCustomerIdFromPrincipal(Principal principal) {
        String email = principal.getName();
        CustomerEntity customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with email: " + email));
        return customer.getId();
    }
}
