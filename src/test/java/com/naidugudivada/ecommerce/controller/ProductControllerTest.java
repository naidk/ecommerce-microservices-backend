package com.naidugudivada.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naidugudivada.ecommerce.domain.product.ProductService;
import com.naidugudivada.ecommerce.domain.product.dto.ProductRequestDTO;
import com.naidugudivada.ecommerce.domain.product.dto.ProductResponseDTO;
import com.naidugudivada.ecommerce.infrastructure.security.JwtAuthenticationFilter;
import com.naidugudivada.ecommerce.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.BeforeAll;
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

import java.util.List;

import static com.naidugudivada.ecommerce.utils.ProductTestUtils.*;
import static com.naidugudivada.ecommerce.utils.TestConstants.ID;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
@WithMockUser(username = "admin", roles = { "ADMIN" })
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private com.naidugudivada.ecommerce.infrastructure.security.JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    static ProductRequestDTO productRequestDTO;
    static ProductResponseDTO productResponseDTO;

    @BeforeAll
    static void setUp() {
        productRequestDTO = createProductRequestDTO();
        productResponseDTO = createProductResponseDTO();
    }

    @Test
    @DisplayName("POST /api/product should return 201 Created")
    void createProductShouldReturnCreated() throws Exception {
        // Arrange
        when(productService.create(any(ProductRequestDTO.class))).thenReturn(productResponseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", is(productResponseDTO.id().toString())));
    }

    @Test
    @DisplayName("GET /api/product/{id} should return 200 OK")
    void findProductByIdShouldReturnOk() throws Exception {
        // Arrange
        when(productService.findById(ID)).thenReturn(productResponseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/product/{id}", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(productResponseDTO.id().toString())));
    }

    @Test
    @DisplayName("GET /api/product should return a page of products")
    void findAllProductsShouldReturnPage() throws Exception {
        // Arrange
        Page<ProductResponseDTO> productPage = new PageImpl<>(List.of(productResponseDTO));
        when(productService.findAll(any(Pageable.class))).thenReturn(productPage);

        // Act & Assert
        mockMvc.perform(get("/api/product"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/product/category/{category} should return a page of products")
    void findProductsByCategoryShouldReturnPage() throws Exception {
        // Arrange
        Page<ProductResponseDTO> productPage = new PageImpl<>(List.of(productResponseDTO));
        var category = productRequestDTO.category();
        when(productService.findAllByCategory(eq(category), any(Pageable.class))).thenReturn(productPage);

        // Act & Assert
        mockMvc.perform(get("/api/product/category/{category}", category))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].category", is(productResponseDTO.category())));
    }

    @Test
    @DisplayName("GET /api/product/label/{label} should return a page of products")
    void findProductsByLabelShouldReturnPage() throws Exception {
        // Arrange
        Page<ProductResponseDTO> productPage = new PageImpl<>(List.of(productResponseDTO));
        var label = productRequestDTO.label();
        when(productService.findAllByLabel(eq(label), any(Pageable.class))).thenReturn(productPage);

        // Act & Assert
        mockMvc.perform(get("/api/product/label/{label}", label))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].label", is(productResponseDTO.label())));
    }

    @Test
    @DisplayName("PUT /api/product/{id} should return 200 OK")
    void updateProductShouldReturnOk() throws Exception {
        // Arrange
        when(productService.updateById(eq(ID), any(ProductRequestDTO.class))).thenReturn(productResponseDTO);

        // Act & Assert
        mockMvc.perform(put("/api/product/{id}", ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(productResponseDTO.name())));
    }

    @Test
    @DisplayName("PATCH /api/product/stock/{id}/{quantity} should return 200 OK")
    void addStockShouldReturnOk() throws Exception {
        // Arrange
        var quantity = productRequestDTO.stockQuantity();
        when(productService.addStockById(ID, quantity)).thenReturn(productResponseDTO);

        // Act & Assert
        mockMvc.perform(patch("/api/product/stock/{id}/{quantity}", ID, quantity))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(productResponseDTO.id().toString())));
    }

    @Test
    @DisplayName("DELETE /api/product/{id} should return 204 No Content")
    void deleteProductShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(productService).deleteById(ID);

        // Act & Assert
        mockMvc.perform(delete("/api/product/{id}", ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/product/{id}/image should return 200 OK")
    void uploadImageShouldReturnOk() throws Exception {
        // Arrange
        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile("file",
                "image.png", MediaType.IMAGE_PNG_VALUE, "image data".getBytes());
        when(productService.uploadImage(eq(ID), any())).thenReturn(productResponseDTO);

        // Act & Assert
        mockMvc.perform(multipart("/api/product/{id}/image", ID).file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(productResponseDTO.id().toString())));
    }

    @Test
    @DisplayName("POST /api/product should return 403 Forbidden for ROLE_USER")
    @WithMockUser(username = "user", roles = { "USER" })
    void createProductShouldReturnForbiddenForUserRole() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequestDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/product/{id} should return 403 Forbidden for ROLE_USER")
    @WithMockUser(username = "user", roles = { "USER" })
    void deleteProductShouldReturnForbiddenForUserRole() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/product/{id}", ID))
                .andExpect(status().isForbidden());
    }
}
