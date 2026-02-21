package com.naidugudivada.ecommerce.domain.shipping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naidugudivada.ecommerce.domain.shipping.dto.ShipmentResponseDTO;
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
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShipmentController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
class ShipmentControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private ShipmentService shipmentService;

        @MockitoBean
        private com.naidugudivada.ecommerce.infrastructure.security.JwtTokenProvider jwtTokenProvider;

        @MockitoBean
        private UserDetailsService userDetailsService;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @WithMockUser
        @DisplayName("GET /api/shipment/order/{orderId} should return tracking details")
        void getTrackingByOrderIdShouldReturnSuccess() throws Exception {
                // Arrange
                UUID orderId = UUID.randomUUID();
                UUID shipmentId = UUID.randomUUID();
                ShipmentResponseDTO mockResponse = ShipmentResponseDTO.builder()
                                .id(shipmentId)
                                .orderId(orderId)
                                .trackingNumber("TBA1234567890")
                                .carrier(CarrierEnum.AMAZON_LOGISTICS)
                                .status(ShipmentStatusEnum.SHIPPED)
                                .estimatedDeliveryDate(LocalDateTime.now().plusDays(3))
                                .build();

                when(shipmentService.getShipmentByOrderId(orderId)).thenReturn(mockResponse);

                // Act & Assert
                mockMvc.perform(get("/api/shipment/order/{orderId}", orderId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.trackingNumber", is("TBA1234567890")))
                                .andExpect(jsonPath("$.status", is("SHIPPED")))
                                .andExpect(jsonPath("$.carrier", is("AMAZON_LOGISTICS")));
        }

        @Test
        @WithMockUser
        @DisplayName("PUT /api/shipment/{shipmentId}/status should update the status")
        void updateShipmentStatusShouldReturnSuccess() throws Exception {
                // Arrange
                UUID shipmentId = UUID.randomUUID();
                UUID orderId = UUID.randomUUID();
                ShipmentResponseDTO mockResponse = ShipmentResponseDTO.builder()
                                .id(shipmentId)
                                .orderId(orderId)
                                .trackingNumber("UPS12345")
                                .carrier(CarrierEnum.UPS)
                                .status(ShipmentStatusEnum.DELIVERED)
                                .estimatedDeliveryDate(LocalDateTime.now().minusDays(1))
                                .build();

                when(shipmentService.updateShipmentStatus(shipmentId, ShipmentStatusEnum.DELIVERED))
                                .thenReturn(mockResponse);

                // Act & Assert
                mockMvc.perform(put("/api/shipment/{shipmentId}/status", shipmentId)
                                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                                .csrf())
                                .param("status", "DELIVERED"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status", is("DELIVERED")));
        }

        @Test
        @DisplayName("Unauthenticated users cannot access shipment routes")
        void unauthenticatedUsersGet401() throws Exception {
                UUID orderId = UUID.randomUUID();
                mockMvc.perform(get("/api/shipment/order/{orderId}", orderId))
                                .andExpect(status().isForbidden());
        }
}
