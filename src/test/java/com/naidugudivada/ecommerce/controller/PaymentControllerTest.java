package com.naidugudivada.ecommerce.controller;

import com.naidugudivada.ecommerce.domain.payment.dto.PaymentProducerResponseDTO;
import com.naidugudivada.ecommerce.domain.payment.kafka.PaymentProducer;
import com.naidugudivada.ecommerce.infrastructure.security.JwtAuthenticationFilter;
import com.naidugudivada.ecommerce.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.naidugudivada.ecommerce.utils.TestConstants.ID;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
@WithMockUser(username = "admin", roles = { "ADMIN" })
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentProducer paymentProducer;

    @MockitoBean
    private com.naidugudivada.ecommerce.infrastructure.security.JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("POST /api/payment/{orderId} should return 200 OK")
    void processPaymentShouldReturnOk() throws Exception {
        // Arrange
        var message = "Payment request sent";
        var responseDTO = new PaymentProducerResponseDTO(message);
        when(paymentProducer.sendPaymentRequest(ID.toString())).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/payment/{orderId}", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is(message)));
    }
}
