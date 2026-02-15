package com.naidugudivada.ecommerce.domain.payment.kafka;

import com.naidugudivada.ecommerce.domain.order.OrderService;
import com.naidugudivada.ecommerce.domain.order.OrderStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import static com.naidugudivada.ecommerce.infrastructure.constants.KafkaConstants.GROUP_ID;
import static com.naidugudivada.ecommerce.infrastructure.constants.KafkaConstants.PAYMENT_RESPONSE_TOPIC;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentConsumer {

    private final OrderService orderService;

    // @KafkaListener(topics = PAYMENT_RESPONSE_TOPIC, groupId = GROUP_ID)
    public void consumePaymentResponse(String orderId) {
        log.info("Received payment confirmation event from Kafka for Order ID: [{}]", orderId);
        orderService.updateStatus(orderId, OrderStatusEnum.PAID);
    }
}
