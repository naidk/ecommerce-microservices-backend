package com.naidugudivada.ecommerce.domain.search;

import com.naidugudivada.ecommerce.domain.product.ProductCategoryEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final ProductSearchRepository productSearchRepository;

    public Page<ProductDocument> searchProducts(String keyword, ProductCategoryEnum category, Pageable pageable) {
        log.info("Searching products with keyword: '{}', category: {}", keyword, category);

        if (StringUtils.isNotBlank(keyword)) {
            // Full-text search on name and label
            return productSearchRepository.findByNameOrLabel(keyword, keyword, pageable);
        } else if (category != null) {
            // Exact match on category
            return productSearchRepository.findByCategory(category, pageable);
        }

        // If no criteria, just return all
        return productSearchRepository.findAll(pageable);
    }
}
