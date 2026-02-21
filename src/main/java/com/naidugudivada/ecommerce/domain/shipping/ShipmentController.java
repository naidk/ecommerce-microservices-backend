package com.naidugudivada.ecommerce.domain.shipping;

import com.naidugudivada.ecommerce.domain.shipping.dto.ShipmentResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shipment")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ShipmentResponseDTO> getTrackingByOrderId(@PathVariable UUID orderId) {
        return ResponseEntity.ok(shipmentService.getShipmentByOrderId(orderId));
    }

    @PutMapping("/{shipmentId}/status")
    public ResponseEntity<ShipmentResponseDTO> updateShipmentStatus(
            @PathVariable UUID shipmentId,
            @RequestParam ShipmentStatusEnum status) {
        return ResponseEntity.ok(shipmentService.updateShipmentStatus(shipmentId, status));
    }
}
