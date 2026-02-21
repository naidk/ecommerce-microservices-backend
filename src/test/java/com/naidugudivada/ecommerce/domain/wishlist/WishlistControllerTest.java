package com.naidugudivada.ecommerce.domain.wishlist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naidugudivada.ecommerce.domain.customer.CustomerEntity;
import com.naidugudivada.ecommerce.domain.customer.CustomerRepository;
import com.naidugudivada.ecommerce.domain.wishlist.dto.WishlistResponseDTO;
import com.naidugudivada.ecommerce.infrastructure.security.JwtAuthenticationFilter;
import com.naidugudivada.ecommerce.infrastructure.security.SecurityConfig;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WishlistController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WishlistService wishlistService;

    @MockitoBean
    private CustomerRepository customerRepository;

    @MockitoBean
    private com.naidugudivada.ecommerce.infrastructure.security.JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/wishlist/{productId} should return 200 OK")
    @WithMockUser(username = "customer@example.com", roles = { "USER" })
    void addProductToWishlistShouldReturnOk() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        CustomerEntity mockCustomer = new CustomerEntity();
        mockCustomer.setId(customerId);
        mockCustomer.setEmail("customer@example.com");

        WishlistResponseDTO responseDTO = WishlistResponseDTO.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .items(new ArrayList<>())
                .build();

        when(customerRepository.findByEmail(eq("customer@example.com"))).thenReturn(Optional.of(mockCustomer));
        when(wishlistService.addProductToWishlist(eq(customerId), eq(productId))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/wishlist/{productId}", productId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()));
    }

    @Test
    @DisplayName("GET /api/wishlist should return 200 OK")
    @WithMockUser(username = "customer@example.com", roles = { "USER" })
    void getWishlistShouldReturnOk() throws Exception {
        UUID customerId = UUID.randomUUID();

        CustomerEntity mockCustomer = new CustomerEntity();
        mockCustomer.setId(customerId);
        mockCustomer.setEmail("customer@example.com");

        WishlistResponseDTO responseDTO = WishlistResponseDTO.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .items(new ArrayList<>())
                .build();

        when(customerRepository.findByEmail(eq("customer@example.com"))).thenReturn(Optional.of(mockCustomer));
        when(wishlistService.getWishlist(eq(customerId))).thenReturn(responseDTO);

        mockMvc.perform(get("/api/wishlist")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()));
    }

    @Test
    @DisplayName("DELETE /api/wishlist/{productId} should return 204 No Content")
    @WithMockUser(username = "customer@example.com", roles = { "USER" })
    void deleteProductShouldReturnNoContent() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        CustomerEntity mockCustomer = new CustomerEntity();
        mockCustomer.setId(customerId);
        mockCustomer.setEmail("customer@example.com");

        when(customerRepository.findByEmail(eq("customer@example.com"))).thenReturn(Optional.of(mockCustomer));
        doNothing().when(wishlistService).removeProductFromWishlist(eq(customerId), eq(productId));

        mockMvc.perform(delete("/api/wishlist/{productId}", productId))
                .andExpect(status().isNoContent());
    }
}
