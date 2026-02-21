package com.naidugudivada.ecommerce.domain.customer.event;

import com.naidugudivada.ecommerce.infrastructure.constants.KafkaConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerEventProducer {

    private final ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider;

    public void publishCustomerRegistered(CustomerRegisteredEvent event) {
        log.info("Publishing CustomerRegisteredEvent: {}", event.getEventId());
        kafkaTemplateProvider.ifAvailable(template -> {
            try {
                template.send(KafkaConstants.CUSTOMER_topic, event.getCustomerId().toString(), event);
            } catch (Exception e) {
                log.error("Failed to publish CustomerRegisteredEvent to Kafka. Error: {}", e.getMessage());
            }
        });
        if (kafkaTemplateProvider.getIfAvailable() == null) {
            log.warn("Kafka is disabled. Skipping CustomerRegisteredEvent for {}", event.getCustomerId());
        }
    }
}
