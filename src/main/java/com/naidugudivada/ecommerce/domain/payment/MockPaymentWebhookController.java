package com.naidugudivada.ecommerce.domain.payment;

import com.naidugudivada.ecommerce.domain.order.OrderService;
import com.naidugudivada.ecommerce.domain.order.OrderStatusEnum;
import com.naidugudivada.ecommerce.domain.shipping.ShipmentService;
import com.naidugudivada.ecommerce.domain.shipping.dto.ShipmentResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/payment/webhook")
@RequiredArgsConstructor
@Slf4j
public class MockPaymentWebhookController {

    private final OrderService orderService;
    private final ShipmentService shipmentService;

    /**
     * Simulates a successful payment webhook from a gateway (e.g., Stripe/Zuplo).
     * This sets the order to PAID and automatically generates the shipment
     * tracking.
     */
    @PostMapping("/success/{orderId}")
    public ResponseEntity<ShipmentResponseDTO> handlePaymentSuccess(@PathVariable UUID orderId) {
        log.info("Mock Webhook invoked: Payment successful for Order [{}]", orderId);

        // 1. Update Order Status
        try {
            orderService.updateStatus(orderId.toString(), OrderStatusEnum.PAID);
        } catch (Exception e) {
            log.error("Order [{}] not found or failed to update status to PAID.", orderId);
            return ResponseEntity.badRequest().build();
        }

        // 2. Generate Shipment
        try {
            ShipmentResponseDTO shipment = shipmentService.createShipment(orderId);
            return ResponseEntity.ok(shipment);
        } catch (Exception e) {
            log.error("Failed to create shipment for Order [{}]: {}", orderId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
