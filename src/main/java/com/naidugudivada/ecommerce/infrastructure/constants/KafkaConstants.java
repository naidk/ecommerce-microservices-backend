package com.naidugudivada.ecommerce.infrastructure.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class KafkaConstants {

    public static final String GROUP_ID = "ecommerce-group";

    public static final String PAYMENT_TOPIC = "payment-topic";
    public static final String PAYMENT_RESPONSE_TOPIC = "payment-response-topic";
    public static final String CLEAR_CART_TOPIC = "clear-cart-topic";
    public static final String CUSTOMER_topic = "customer-events";
    public static final String PRODUCT_TOPIC = "product-events";
    public static final String ORDER_TOPIC = "order-events";

    public static final String PAYMENT_REQUEST_IS_NOW_BEING_PROCESSED = "Payment request is now being processed.";
}
