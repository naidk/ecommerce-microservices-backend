package com.naidugudivada.ecommerce.domain.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naidugudivada.ecommerce.domain.product.ProductCategoryEnum;
import com.naidugudivada.ecommerce.infrastructure.security.JwtAuthenticationFilter;
import com.naidugudivada.ecommerce.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchService searchService;

    @MockitoBean
    private com.naidugudivada.ecommerce.infrastructure.security.JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/search with keyword should return matching products")
    void searchProductsWithKeywordShouldReturnPage() throws Exception {
        // Arrange
        UUID productId = UUID.randomUUID();
        ProductDocument doc = ProductDocument.builder()
                .id(productId)
                .name("Laptop Pro")
                .label("test-label")
                .category(ProductCategoryEnum.LAPTOP)
                .price(BigDecimal.valueOf(1000.00))
                .build();
        Page<ProductDocument> productPage = new PageImpl<>(List.of(doc));

        when(searchService.searchProducts(eq("Laptop"), eq(null), any(Pageable.class))).thenReturn(productPage);

        // Act & Assert
        // Search endpoints are public according to SecurityConfig
        mockMvc.perform(get("/api/search")
                .param("q", "Laptop")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Laptop Pro")));
    }

    @Test
    @DisplayName("GET /api/search with category should return matching products")
    void searchProductsWithCategoryShouldReturnPage() throws Exception {
        // Arrange
        UUID productId = UUID.randomUUID();
        ProductDocument doc = ProductDocument.builder()
                .id(productId)
                .name("Running Shoes")
                .label("test-label")
                .category(ProductCategoryEnum.SMARTPHONE)
                .price(BigDecimal.valueOf(100.00))
                .build();
        Page<ProductDocument> productPage = new PageImpl<>(List.of(doc));

        when(searchService.searchProducts(eq(null), eq(ProductCategoryEnum.SMARTPHONE), any(Pageable.class)))
                .thenReturn(productPage);

        // Act & Assert
        mockMvc.perform(get("/api/search")
                .param("category", "SMARTPHONE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].category", is("SMARTPHONE")));
    }
}
