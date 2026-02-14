package com.naidugudivada.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naidugudivada.ecommerce.domain.shoppingcart.ShoppingCartService;
import com.naidugudivada.ecommerce.domain.shoppingcart.dto.ShoppingCartRequestDTO;
import com.naidugudivada.ecommerce.domain.shoppingcart.dto.ShoppingCartResponseDTO;
import com.naidugudivada.ecommerce.infrastructure.security.JwtAuthenticationFilter;
import com.naidugudivada.ecommerce.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.naidugudivada.ecommerce.utils.ShoppingCartTestUtils.*;
import static com.naidugudivada.ecommerce.utils.TestConstants.ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShoppingCartController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
@WithMockUser(username = "admin", roles = { "ADMIN" })
class ShoppingCartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShoppingCartService shoppingCartService;

    @MockitoBean
    private com.naidugudivada.ecommerce.infrastructure.security.JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    static ShoppingCartRequestDTO shoppingCartRequestDTO;
    static ShoppingCartResponseDTO shoppingCartResponseDTO;

    @BeforeAll
    static void setUp() {
        shoppingCartRequestDTO = createShoppingCartRequestDTO();
        shoppingCartResponseDTO = createShoppingCartResponseDTO();
    }

    @Test
    @DisplayName("GET /api/shopping-cart/{customerId} should return 200 OK")
    void getShoppingCartShouldReturnOk() throws Exception {
        // Arrange
        when(shoppingCartService.getShoppingCartByCustomerId(ID)).thenReturn(shoppingCartResponseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/shopping-cart/{customerId}", ID))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(shoppingCartResponseDTO)));
    }

    @Test
    @DisplayName("POST /api/shopping-cart/{customerId} should return 200 OK")
    void addToCartShouldReturnOk() throws Exception {
        // Arrange
        when(shoppingCartService.addToCart(eq(ID), any(ShoppingCartRequestDTO.class)))
                .thenReturn(shoppingCartResponseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/shopping-cart/{customerId}", ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shoppingCartRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(shoppingCartResponseDTO)));
    }

    @Test
    @DisplayName("DELETE /api/shopping-cart/{customerId} should return 200 OK")
    void removeFromCartShouldReturnOk() throws Exception {
        // Arrange
        when(shoppingCartService.removeFromCart(eq(ID), any(ShoppingCartRequestDTO.class)))
                .thenReturn(shoppingCartResponseDTO);

        // Act & Assert
        mockMvc.perform(delete("/api/shopping-cart/{customerId}", ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shoppingCartRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(shoppingCartResponseDTO)));
    }
}
