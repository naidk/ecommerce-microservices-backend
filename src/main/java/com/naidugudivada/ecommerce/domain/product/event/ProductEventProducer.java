package com.naidugudivada.ecommerce.domain.product.event;

import com.naidugudivada.ecommerce.infrastructure.constants.KafkaConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishProductCreated(ProductCreatedEvent event) {
        log.info("Publishing ProductCreatedEvent: {}", event.getEventId());
        kafkaTemplate.send(KafkaConstants.PRODUCT_TOPIC, event.getProductId().toString(), event);
    }

    public void publishStockUpdated(StockUpdatedEvent event) {
        log.info("Publishing StockUpdatedEvent: {}", event.getEventId());
        kafkaTemplate.send(KafkaConstants.PRODUCT_TOPIC, event.getProductId().toString(), event);
    }
}
