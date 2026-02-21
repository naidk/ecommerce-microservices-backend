package com.naidugudivada.ecommerce.domain.shipping;

import com.naidugudivada.ecommerce.domain.order.OrderEntity;
import com.naidugudivada.ecommerce.domain.order.OrderRepository;
import com.naidugudivada.ecommerce.domain.order.exceptions.OrderNotFoundException;
import com.naidugudivada.ecommerce.domain.shipping.dto.ShipmentResponseDTO;
import com.naidugudivada.ecommerce.domain.shipping.exceptions.ShipmentNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.ORDER_NOT_FOUND_WITH_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final ShipmentMapper shipmentMapper;

    // A mock carrier list for random assignment
    private static final List<CarrierEnum> CARRIERS = List.of(
            CarrierEnum.AMAZON_LOGISTICS,
            CarrierEnum.UPS,
            CarrierEnum.FEDEX,
            CarrierEnum.USPS);
    private final Random random = new Random();

    @Transactional
    public ShipmentResponseDTO createShipment(UUID orderId) {
        log.info("Creating shipment for Order [{}]", orderId);

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(String.format(ORDER_NOT_FOUND_WITH_ID, orderId)));

        // Ensure we don't duplicate a shipment if it somehow retries
        var existingShipment = shipmentRepository.findByOrderId(orderId);
        if (existingShipment.isPresent()) {
            log.info("Shipment already exists for Order [{}]. Returning existing.", orderId);
            return shipmentMapper.toResponseDTO(existingShipment.get());
        }

        // Mock generate a tracking number (e.g., TBA1234567890)
        String trackingNumber = "TBA" + (1000000000L + random.nextLong(9000000000L));
        CarrierEnum assignedCarrier = CARRIERS.get(random.nextInt(CARRIERS.size()));

        // Mock estimate 3-5 days delivery
        int deliveryDays = 3 + random.nextInt(3);

        ShipmentEntity shipment = ShipmentEntity.builder()
                .order(order)
                .trackingNumber(trackingNumber)
                .carrier(assignedCarrier)
                .status(ShipmentStatusEnum.PENDING)
                .estimatedDeliveryDate(LocalDateTime.now().plusDays(deliveryDays))
                .build();

        ShipmentEntity savedShipment = shipmentRepository.save(shipment);
        log.info("Successfully created tracking [{}] via [{}] for Order [{}]", trackingNumber, assignedCarrier,
                orderId);

        return shipmentMapper.toResponseDTO(savedShipment);
    }

    @Transactional
    public ShipmentResponseDTO updateShipmentStatus(UUID shipmentId, ShipmentStatusEnum newStatus) {
        ShipmentEntity shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found with ID: " + shipmentId));

        log.info("Updating shipment [{}] status from [{}] to [{}]", shipmentId, shipment.getStatus(), newStatus);
        shipment.setStatus(newStatus);

        return shipmentMapper.toResponseDTO(shipmentRepository.save(shipment));
    }

    @Transactional(readOnly = true)
    public ShipmentResponseDTO getShipmentByOrderId(UUID orderId) {
        return shipmentRepository.findByOrderId(orderId)
                .map(shipmentMapper::toResponseDTO)
                .orElseThrow(() -> new ShipmentNotFoundException("No shipment found for order: " + orderId));
    }
}
