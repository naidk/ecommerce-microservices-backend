package com.naidugudivada.ecommerce.domain.product;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
    Page<ProductEntity> findAllByActiveTrue(Pageable pageable);

    Optional<ProductEntity> findByIdAndActiveTrue(UUID id);

    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.active = true")
    Page<ProductEntity> findAllByCategoryIgnoreCaseAndActiveTrue(@Param("category") ProductCategoryEnum category,
            Pageable pageable);

    Page<ProductEntity> findAllByLabelIgnoreCaseAndActiveTrue(String label, Pageable pageable);

    boolean existsBySku(String sku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.active = true")
    Optional<ProductEntity> findByIdWithLock(UUID id);
}
