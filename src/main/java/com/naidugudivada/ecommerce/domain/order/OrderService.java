package com.naidugudivada.ecommerce.domain.order;

import com.naidugudivada.ecommerce.domain.address.AddressEntity;
import com.naidugudivada.ecommerce.domain.address.AddressTypeEnum;
import com.naidugudivada.ecommerce.domain.address.exceptions.AddressNotFoundException;
import com.naidugudivada.ecommerce.domain.idempotency.IdempotencyEntity;
import com.naidugudivada.ecommerce.domain.idempotency.IdempotencyRepository;
import com.naidugudivada.ecommerce.domain.order.dto.OrderResponseDTO;
import com.naidugudivada.ecommerce.domain.order.exceptions.EmptyShoppingCartException;
import com.naidugudivada.ecommerce.domain.order.exceptions.OrderNotFoundException;
import com.naidugudivada.ecommerce.domain.order.event.OrderCreatedEvent;
import com.naidugudivada.ecommerce.domain.order.event.OrderEventProducer;
import com.naidugudivada.ecommerce.domain.order.orderaddress.OrderAddressEntity;
import com.naidugudivada.ecommerce.domain.order.orderitem.OrderItemEntity;
import com.naidugudivada.ecommerce.domain.shoppingcart.ShoppingCartEntity;
import com.naidugudivada.ecommerce.domain.shoppingcart.ShoppingCartService;
import com.naidugudivada.ecommerce.infrastructure.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.ADDRESS_NOT_FOUND;
import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.ORDER_NOT_FOUND_WITH_ID;
import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.SHOPPING_CART_IS_EMPTY;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ShoppingCartService shoppingCartService;
    private final OrderMapper orderMapper;
    private final OrderEventProducer orderEventProducer;
    private final IdempotencyRepository idempotencyRepository;
    private final JsonUtils json;

    @Transactional(readOnly = true)
    public OrderResponseDTO findById(UUID id) {
        return orderMapper.toResponseDTO(getEntity(id));
    }

    @Transactional
    public OrderResponseDTO purchaseShoppingCart(UUID customerId, String idempotencyKey) {
        log.info("Initiating Checkout for Customer: [{}]", customerId);
        var key = UUID.fromString(idempotencyKey);

        var existing = idempotencyRepository.findById(key);
        if (existing.isPresent()) {
            log.info("Idempotency key hit [{}]. Returning cached order response.", key);
            return json.fromJson(existing.get().getResponseJson(), OrderResponseDTO.class);
        }

        var shoppingCart = shoppingCartService.findByCustomerId(customerId);
        if (shoppingCart.getItems().isEmpty()) {
            log.warn("Checkout failed. Shopping cart is empty for Customer: [{}]", customerId);
            throw new EmptyShoppingCartException(SHOPPING_CART_IS_EMPTY);
        }

        var shippingAddress = getShippingAddress(shoppingCart);
        var orderItems = orderMapper.toOrderItemsEntity(shoppingCart.getItems());
        var orderAddress = orderMapper.toOrderAddressEntity(shippingAddress);
        var order = buildOrder(shoppingCart, orderItems, orderAddress);

        order.getOrderItems().forEach(item -> item.setOrder(order));
        orderRepository.save(order);

        log.info("Order entity persisted. ID: [{}], Total: {}", order.getId(), order.getTotalPrice());

        var response = orderMapper.toResponseDTO(order);
        var responseJson = json.toJson(response);

        try {
            idempotencyRepository.saveAndFlush(
                    IdempotencyEntity.builder()
                            .idempotencyKey(key)
                            .responseJson(responseJson)
                            .build());

            log.info("Checkout complete. Clearing cart for Customer: [{}]", customerId);

            orderEventProducer.publishOrderCreated(
                    OrderCreatedEvent.builder()
                            .orderId(order.getId())
                            .customerId(shoppingCart.getCustomer().getId())
                            .totalAmount(order.getTotalPrice())
                            .source("OrderService")
                            .eventType("OrderCreated")
                            .build());

            orderEventProducer.publishClearCart(String.valueOf(customerId));
            return response;

        } catch (DataIntegrityViolationException e) {
            log.info("Concurrent transaction detected for Idempotency Key [{}]. Recovering cached response.", key);
            var exceptionResponse = idempotencyRepository.findById(key)
                    .map(entity -> json.fromJson(entity.getResponseJson(), OrderResponseDTO.class))
                    .orElseThrow(() -> e);

            orderEventProducer.publishClearCart(String.valueOf(customerId));
            return exceptionResponse;
        }
    }

    @Transactional
    public void updateStatus(String id, OrderStatusEnum status) {
        log.info("Updating status for Order [{}] to [{}]", id, status);
        var order = getEntity(UUID.fromString(id));
        order.setStatus(status);
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> findAllByCustomerId(UUID customerId, Pageable pageable) {
        return orderRepository.findAllByCustomerId(customerId, pageable)
                .map(orderMapper::toResponseDTO);
    }

    public OrderEntity getEntity(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(String.format(ORDER_NOT_FOUND_WITH_ID, id)));
    }

    private AddressEntity getShippingAddress(ShoppingCartEntity shoppingCart) {
        return shoppingCart.getCustomer().getAddress().stream()
                .filter(a -> a.getType().equals(AddressTypeEnum.SHIPPING))
                .findFirst()
                .orElseGet(() -> shoppingCart.getCustomer().getAddress().stream()
                        .filter(a -> a.getType().equals(AddressTypeEnum.BILLING))
                        .findFirst()
                        .orElseThrow(() -> new AddressNotFoundException(ADDRESS_NOT_FOUND)));
    }

    private OrderEntity buildOrder(ShoppingCartEntity shoppingCart, List<OrderItemEntity> orderItems,
            OrderAddressEntity orderAddress) {
        return OrderEntity.builder()
                .customer(shoppingCart.getCustomer())
                .orderItems(orderItems)
                .shippingAddress(orderAddress)
                .totalPrice(shoppingCart.getTotalPrice())
                .status(OrderStatusEnum.PENDING)
                .build();
    }
}
