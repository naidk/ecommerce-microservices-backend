package com.naidugudivada.ecommerce.domain.search;

import com.naidugudivada.ecommerce.domain.product.ProductCategoryEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// @RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<Page<ProductDocument>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) ProductCategoryEnum category,
            Pageable pageable) {
        return ResponseEntity.ok(searchService.searchProducts(q, category, pageable));
    }
}
