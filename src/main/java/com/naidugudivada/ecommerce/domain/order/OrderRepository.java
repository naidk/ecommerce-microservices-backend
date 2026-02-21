package com.naidugudivada.ecommerce.domain.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
    Page<OrderEntity> findAllByCustomerId(UUID customerId, Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o JOIN o.orderItems oi WHERE o.customer.id = :customerId AND oi.product.id = :productId AND o.status IN ('PAID', 'SHIPPED', 'DELIVERED')")
    long countPurchasesByCustomerAndProduct(@Param("customerId") UUID customerId, @Param("productId") UUID productId);
}
