package com.naidugudivada.ecommerce.domain.notification;

import com.naidugudivada.ecommerce.domain.customer.event.CustomerRegisteredEvent;
import com.naidugudivada.ecommerce.domain.order.event.OrderCreatedEvent;
import com.naidugudivada.ecommerce.domain.product.event.ProductCreatedEvent;
import com.naidugudivada.ecommerce.domain.product.event.StockUpdatedEvent;
import com.naidugudivada.ecommerce.infrastructure.constants.KafkaConstants;
import com.naidugudivada.ecommerce.service.AwsSesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final AwsSesService awsSesService;

    // @KafkaListener(topics = KafkaConstants.CUSTOMER_topic, groupId =
    // "${spring.kafka.consumer.group-id}")
    public void handleCustomerRegistered(CustomerRegisteredEvent event) {
        log.info("NOTIFICATION SERVICE: Welcome email processing for {} (Customer ID: {})", event.getEmail(),
                event.getCustomerId());

        String subject = "Welcome to Our Store!";
        String body = "<h1>Welcome onboard!</h1><p>Thank you for registering with us. Enjoy shopping!</p>";
        awsSesService.sendEmail(event.getEmail(), subject, body);
    }

    // @KafkaListener(topics = KafkaConstants.PRODUCT_TOPIC, groupId =
    // "${spring.kafka.consumer.group-id}")
    public void handleProductEvents(Object event) {
        if (event instanceof ProductCreatedEvent productEvent) {
            log.info("NOTIFICATION SERVICE: New product alert! {} (SKU: {}) available for ${}", productEvent.getName(),
                    productEvent.getSku(), productEvent.getPrice());
        } else if (event instanceof StockUpdatedEvent stockEvent) {
            log.info("NOTIFICATION SERVICE: Stock updated for Product ID: {}. New Quantity: {}",
                    stockEvent.getProductId(), stockEvent.getNewQuantity());
        } else {
            log.warn("NOTIFICATION SERVICE: Unknown product event received: {}", event);
        }
    }

    // @KafkaListener(topics = KafkaConstants.ORDER_TOPIC, groupId =
    // "${spring.kafka.consumer.group-id}")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("NOTIFICATION SERVICE: Order Confirmation processing for Order ID: {}. Total: {}", event.getOrderId(),
                event.getTotalAmount());

        // Use placeholder recipient until order contains true customer email.
        String customerEmail = "naidugudivada766@gmail.com";

        String subject = "Order Confirmation - #" + event.getOrderId();
        String body = "<h1>Thank You for Your Order!</h1>" +
                "<p>Your total amount is <b>$" + event.getTotalAmount() + "</b>.</p>" +
                "<p>We are processing it quickly and will notify you when it ships.</p>";

        awsSesService.sendEmail(customerEmail, subject, body);
    }
}
