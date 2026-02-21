package com.naidugudivada.ecommerce.domain.wishlist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistEntity, UUID> {
    Optional<WishlistEntity> findByCustomerId(UUID customerId);
}
