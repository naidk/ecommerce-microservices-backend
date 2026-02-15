package com.naidugudivada.ecommerce.domain.order.kafka;

import com.naidugudivada.ecommerce.domain.shoppingcart.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static com.naidugudivada.ecommerce.infrastructure.constants.KafkaConstants.CLEAR_CART_TOPIC;
import static com.naidugudivada.ecommerce.infrastructure.constants.KafkaConstants.GROUP_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final ShoppingCartService shoppingCartService;

    @Transactional
    // @KafkaListener(topics = CLEAR_CART_TOPIC, groupId = GROUP_ID)
    public void consumeClearCartRequest(String customerId) {
        log.info("Received clear cart request for Customer ID: [{}]", customerId);

        var shoppingCart = shoppingCartService.findByCustomerId(UUID.fromString(customerId));

        int itemCount = shoppingCart.getItems().size();
        shoppingCart.getItems().clear();
        shoppingCart.setTotalPrice(BigDecimal.ZERO);

        shoppingCartService.save(shoppingCart);

        log.debug("Shopping cart cleared successfully for Customer ID: [{}]. Removed {} items.", customerId, itemCount);
    }
}
