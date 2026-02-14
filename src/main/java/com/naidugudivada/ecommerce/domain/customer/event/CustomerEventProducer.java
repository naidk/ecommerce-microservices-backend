package com.naidugudivada.ecommerce.domain.customer.event;

import com.naidugudivada.ecommerce.infrastructure.constants.KafkaConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCustomerRegistered(CustomerRegisteredEvent event) {
        log.info("Publishing CustomerRegisteredEvent: {}", event.getEventId());
        kafkaTemplate.send(KafkaConstants.CUSTOMER_topic, event.getCustomerId().toString(), event);
    }
}
