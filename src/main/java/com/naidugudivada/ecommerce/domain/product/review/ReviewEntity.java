package com.naidugudivada.ecommerce.domain.product.review;

import com.naidugudivada.ecommerce.domain.BaseEntity;
import com.naidugudivada.ecommerce.domain.customer.CustomerEntity;
import com.naidugudivada.ecommerce.domain.product.ProductEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity(name = "Review")
@Table(name = "product_review", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "product_id", "customer_id" })
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ReviewEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @Column(nullable = false)
    private Integer rating;

    @Column
    private String title;

    @Column(columnDefinition = "TEXT")
    private String comment;
}
