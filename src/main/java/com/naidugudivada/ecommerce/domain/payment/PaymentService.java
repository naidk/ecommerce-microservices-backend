package com.naidugudivada.ecommerce.domain.payment;

import com.naidugudivada.ecommerce.domain.order.OrderService;
import com.naidugudivada.ecommerce.domain.order.OrderStatusEnum;
import com.naidugudivada.ecommerce.domain.payment.dto.PaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static com.naidugudivada.ecommerce.infrastructure.constants.KafkaConstants.GROUP_ID;
import static com.naidugudivada.ecommerce.infrastructure.constants.KafkaConstants.PAYMENT_RESPONSE_TOPIC;
import static com.naidugudivada.ecommerce.infrastructure.constants.KafkaConstants.PAYMENT_TOPIC;

@Service
@Slf4j
public class PaymentService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final WebClient webClient;
    private final OrderService orderService;

    public PaymentService(@Value("${payment.api.url}") String paymentApiUrl,
            KafkaTemplate<String, Object> kafkaTemplate, OrderService orderService) {
        this.kafkaTemplate = kafkaTemplate;
        this.webClient = WebClient.create(paymentApiUrl);
        this.orderService = orderService;
    }

    @KafkaListener(topics = PAYMENT_TOPIC, groupId = GROUP_ID)
    public void processPayment(String orderId) {
        log.info("Received payment processing event for Order ID: [{}]", orderId);

        var body = new PaymentRequest(orderId);

        try {
            webClient.post()
                    .uri("/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Payment authorized successfully for Order ID: [{}]. Notifying system.", orderId);
            kafkaTemplate.send(PAYMENT_RESPONSE_TOPIC, orderId, orderId);

        } catch (WebClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                log.warn("Payment declined for Order ID: [{}]. Reason: {}", orderId, e.getMessage());
                orderService.updateStatus(orderId, OrderStatusEnum.FAILED);
            } else {
                log.error("Payment API returned server error (5xx) for Order ID: [{}]", orderId);
                throw e;
            }
        } catch (Exception e) {
            log.error("Critical failure contacting Payment API for Order ID: [{}]", orderId, e);
            throw e;
        }
    }
}
