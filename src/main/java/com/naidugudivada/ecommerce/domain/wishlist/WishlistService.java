package com.naidugudivada.ecommerce.domain.wishlist;

import com.naidugudivada.ecommerce.domain.customer.CustomerEntity;
import com.naidugudivada.ecommerce.domain.customer.CustomerRepository;
import com.naidugudivada.ecommerce.domain.product.ProductEntity;
import com.naidugudivada.ecommerce.domain.product.ProductRepository;
import com.naidugudivada.ecommerce.domain.wishlist.dto.WishlistResponseDTO;
import com.naidugudivada.ecommerce.domain.wishlist.exceptions.WishlistException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.CUSTOMER_NOT_FOUND_WITH_ID;
import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.PRODUCT_NOT_FOUND_WITH_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final WishlistMapper wishlistMapper;

    @Transactional
    public WishlistResponseDTO addProductToWishlist(UUID customerId, UUID productId) {
        WishlistEntity wishlist = getOrCreateWishlist(customerId);

        if (wishlistItemRepository.existsByWishlistIdAndProductId(wishlist.getId(), productId)) {
            throw new WishlistException("Product is already in the wishlist.");
        }

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(PRODUCT_NOT_FOUND_WITH_ID, productId)));

        WishlistItemEntity item = WishlistItemEntity.builder()
                .wishlist(wishlist)
                .product(product)
                .addedAt(LocalDateTime.now())
                .build();

        wishlist.getItems().add(item);
        wishlistRepository.save(wishlist);
        log.info("Added product {} to wishlist for customer {}", productId, customerId);

        return wishlistMapper.toResponseDTO(wishlist);
    }

    @Transactional
    public void removeProductFromWishlist(UUID customerId, UUID productId) {
        WishlistEntity wishlist = getOrCreateWishlist(customerId);
        wishlistItemRepository.deleteByWishlistIdAndProductId(wishlist.getId(), productId);
        log.info("Removed product {} from wishlist for customer {}", productId, customerId);
    }

    @Transactional(readOnly = true)
    public WishlistResponseDTO getWishlist(UUID customerId) {
        return wishlistMapper.toResponseDTO(getOrCreateWishlist(customerId));
    }

    protected WishlistEntity getOrCreateWishlist(UUID customerId) {
        return wishlistRepository.findByCustomerId(customerId).orElseGet(() -> {
            CustomerEntity customer = customerRepository.findById(customerId)
                    .orElseThrow(
                            () -> new IllegalArgumentException(String.format(CUSTOMER_NOT_FOUND_WITH_ID, customerId)));
            WishlistEntity newWishlist = WishlistEntity.builder()
                    .customer(customer)
                    .build();
            return wishlistRepository.save(newWishlist);
        });
    }
}
