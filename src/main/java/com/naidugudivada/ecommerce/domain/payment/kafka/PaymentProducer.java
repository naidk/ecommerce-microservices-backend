package com.naidugudivada.ecommerce.domain.payment.kafka;

import com.naidugudivada.ecommerce.domain.idempotency.IdempotencyEntity;
import com.naidugudivada.ecommerce.domain.idempotency.IdempotencyRepository;
import com.naidugudivada.ecommerce.domain.order.OrderService;
import com.naidugudivada.ecommerce.domain.order.OrderStatusEnum;
import com.naidugudivada.ecommerce.domain.payment.dto.PaymentProducerResponseDTO;
import com.naidugudivada.ecommerce.domain.payment.exceptions.PaymentStatusException;
import com.naidugudivada.ecommerce.infrastructure.utils.JsonUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.ORDER_STATUS_MUST_BE_PENDING;
import static com.naidugudivada.ecommerce.infrastructure.constants.KafkaConstants.PAYMENT_REQUEST_IS_NOW_BEING_PROCESSED;
import static com.naidugudivada.ecommerce.infrastructure.constants.KafkaConstants.PAYMENT_TOPIC;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderService orderService;
    private final IdempotencyRepository idempotencyRepository;
    private final JsonUtils json;

    @Transactional
    public PaymentProducerResponseDTO sendPaymentRequest(String orderId) {
        var key = UUID.fromString(orderId);

        var existing = idempotencyRepository.findById(key);
        if (existing.isPresent()) {
            log.info("Idempotency hit: Payment request already processed for Order [{}]. Returning cached response.",
                    orderId);
            return json.fromJson(existing.get().getResponseJson(), PaymentProducerResponseDTO.class);
        }

        var order = orderService.getEntity(key);
        if (ObjectUtils.notEqual(order.getStatus(), OrderStatusEnum.PENDING)) {
            log.warn("Payment request rejected for Order [{}]. Current status is [{}], but expected PENDING.", orderId,
                    order.getStatus());
            throw new PaymentStatusException(ORDER_STATUS_MUST_BE_PENDING);
        }

        log.info("Publishing payment request event to Kafka topic [{}] for Order [{}]", PAYMENT_TOPIC, orderId);
        kafkaTemplate.send(PAYMENT_TOPIC, orderId, orderId);

        var response = new PaymentProducerResponseDTO(PAYMENT_REQUEST_IS_NOW_BEING_PROCESSED);
        var responseJson = json.toJson(response);

        try {
            idempotencyRepository.saveAndFlush(
                    IdempotencyEntity.builder()
                            .idempotencyKey(key)
                            .responseJson(responseJson)
                            .build());
            return response;

        } catch (DataIntegrityViolationException e) {
            log.info("Concurrent payment request detected for Order [{}]. Recovering cached response.", orderId);
            return idempotencyRepository.findById(key)
                    .map(entity -> json.fromJson(entity.getResponseJson(), PaymentProducerResponseDTO.class))
                    .orElseThrow(() -> e);
        }
    }
}
