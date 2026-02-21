package com.naidugudivada.ecommerce.domain.product.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {
    Page<ReviewEntity> findByProductId(UUID productId, Pageable pageable);

    boolean existsByProductIdAndCustomerId(UUID productId, UUID customerId);
}
