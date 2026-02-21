package com.naidugudivada.ecommerce.domain.product.event;

import com.naidugudivada.ecommerce.infrastructure.constants.KafkaConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventProducer {

    private final ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider;

    public void publishProductCreated(ProductCreatedEvent event) {
        log.info("Publishing ProductCreatedEvent: {}", event.getEventId());
        kafkaTemplateProvider.ifAvailable(template -> {
            try {
                template.send(KafkaConstants.PRODUCT_TOPIC, event.getProductId().toString(), event);
            } catch (Exception e) {
                log.error("Failed to publish ProductCreatedEvent for productId: {}. Error: {}", event.getProductId(),
                        e.getMessage());
            }
        });
    }

    public void publishStockUpdated(StockUpdatedEvent event) {
        log.info("Publishing StockUpdatedEvent: {}", event.getEventId());
        kafkaTemplateProvider.ifAvailable(template -> {
            try {
                template.send(KafkaConstants.PRODUCT_TOPIC, event.getProductId().toString(), event);
            } catch (Exception e) {
                log.error("Failed to publish StockUpdatedEvent for productId: {}. Error: {}", event.getProductId(),
                        e.getMessage());
            }
        });
    }
}
