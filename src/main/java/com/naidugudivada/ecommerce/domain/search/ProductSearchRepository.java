package com.naidugudivada.ecommerce.domain.search;

import com.naidugudivada.ecommerce.domain.product.ProductCategoryEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, UUID> {
    Page<ProductDocument> findByNameOrLabel(String name, String label, Pageable pageable);

    Page<ProductDocument> findByCategory(ProductCategoryEnum category, Pageable pageable);
}
