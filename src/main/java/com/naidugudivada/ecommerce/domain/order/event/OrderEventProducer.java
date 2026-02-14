package com.naidugudivada.ecommerce.domain.order.event;

import com.naidugudivada.ecommerce.infrastructure.constants.KafkaConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent: {}", event.getEventId());
        kafkaTemplate.send(KafkaConstants.ORDER_TOPIC, event.getOrderId().toString(), event);
    }

    public void publishClearCart(String customerId) {
        log.info("Publishing Clear Cart Event for Customer: {}", customerId);
        kafkaTemplate.send(KafkaConstants.CLEAR_CART_TOPIC, customerId, customerId);
    }
}
