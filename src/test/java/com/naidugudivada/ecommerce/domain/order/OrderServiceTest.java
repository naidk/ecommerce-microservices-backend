package com.naidugudivada.ecommerce.domain.order;

import com.naidugudivada.ecommerce.domain.address.AddressTypeEnum;
import com.naidugudivada.ecommerce.domain.idempotency.IdempotencyEntity;
import com.naidugudivada.ecommerce.domain.idempotency.IdempotencyRepository;
import com.naidugudivada.ecommerce.domain.order.exceptions.EmptyShoppingCartException;
import com.naidugudivada.ecommerce.domain.order.exceptions.OrderNotFoundException;
import com.naidugudivada.ecommerce.domain.order.dto.OrderResponseDTO;
import com.naidugudivada.ecommerce.domain.order.event.OrderCreatedEvent;
import com.naidugudivada.ecommerce.domain.order.event.OrderEventProducer;
import com.naidugudivada.ecommerce.domain.shoppingcart.ShoppingCartEntity;
import com.naidugudivada.ecommerce.domain.shoppingcart.ShoppingCartService;
import com.naidugudivada.ecommerce.infrastructure.utils.JsonUtils;
import com.naidugudivada.ecommerce.utils.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.naidugudivada.ecommerce.utils.OrderTestUtils.*;
import static com.naidugudivada.ecommerce.utils.ShoppingCartTestUtils.*;
import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.ORDER_NOT_FOUND_WITH_ID;
import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.SHOPPING_CART_IS_EMPTY;
import static com.naidugudivada.ecommerce.utils.TestConstants.ID;
import static com.naidugudivada.ecommerce.utils.TestConstants.IDEMPOTENCY_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ShoppingCartService shoppingCartService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderEventProducer orderEventProducer;

    @Mock
    private Pageable pageable;

    @Mock
    private IdempotencyRepository idempotencyRepository;

    @Mock
    private JsonUtils json;

    @InjectMocks
    private OrderService orderService;

    private OrderEntity orderEntity;
    private OrderResponseDTO orderResponseDTO;
    private ShoppingCartEntity shoppingCartEntity;

    @BeforeEach
    void setUp() {
        orderEntity = createOrderEntity();
        orderResponseDTO = createOrderResponseDTO();
        shoppingCartEntity = createShoppingCartEntity();
    }

    @Test
    void testFindByIdSuccess() {
        // Arrange
        when(orderRepository.findById(orderEntity.getId())).thenReturn(Optional.of(orderEntity));
        when(orderMapper.toResponseDTO(orderEntity)).thenReturn(orderResponseDTO);

        // Act
        var result = orderService.findById(ID);

        // Assert
        assertThat(result).isNotNull().isEqualTo(orderResponseDTO);
        verify(orderRepository).findById(orderEntity.getId());
        verify(orderMapper).toResponseDTO(orderEntity);
    }

    @Test
    void testFindByIdNotFound() {
        // Arrange
        when(orderRepository.findById(orderEntity.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatException()
                .isThrownBy(() -> orderService.findById(ID))
                .isInstanceOf(OrderNotFoundException.class)
                .withMessageContaining(String.format(ORDER_NOT_FOUND_WITH_ID, ID));
        verify(orderRepository).findById(orderEntity.getId());
    }

    @Test
    void testFillAllByCustomerId() {
        // Arrange
        Page<OrderEntity> entityPage = new PageImpl<>(List.of(orderEntity));
        when(orderRepository.findAllByCustomerId(ID, pageable)).thenReturn(entityPage);
        when(orderMapper.toResponseDTO(orderEntity)).thenReturn(orderResponseDTO);

        // Act
        Page<OrderResponseDTO> response = orderService.findAllByCustomerId(ID, pageable);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getContent()).containsExactly(orderResponseDTO);
        verify(orderRepository).findAllByCustomerId(ID, pageable);
        verify(orderMapper).toResponseDTO(orderEntity);
    }

    @Test
    void testPurchaseShoppingCartSuccess() {
        // Arrange
        var shippingAddress = shoppingCartEntity.getCustomer().getAddress().get(0);

        when(idempotencyRepository.findById(ID)).thenReturn(Optional.empty());
        when(shoppingCartService.findByCustomerId(ID)).thenReturn(shoppingCartEntity);
        when(orderMapper.toOrderItemsEntity(shoppingCartEntity.getItems())).thenReturn(orderEntity.getOrderItems());
        when(orderMapper.toOrderAddressEntity(shippingAddress)).thenReturn(createOrderAddressEntity());
        when(orderMapper.toResponseDTO(any(OrderEntity.class))).thenReturn(orderResponseDTO);
        when(json.toJson(orderResponseDTO)).thenReturn("{\"id\":\"123\"}");

        // Act
        var result = orderService.purchaseShoppingCart(ID, ID.toString());

        // Assert
        assertThat(result).isNotNull().isEqualTo(orderResponseDTO);
        verify(idempotencyRepository).findById(ID);
        verify(idempotencyRepository).saveAndFlush(any(IdempotencyEntity.class));
        verify(orderRepository).save(any(OrderEntity.class));
        verify(orderEventProducer).publishOrderCreated(any(OrderCreatedEvent.class));
        verify(orderEventProducer).publishClearCart(String.valueOf(ID));
    }

    @Test
    void testPurchaseShoppingCartReturnsStoredResponseWhenIdempotencyRecordExists() {
        // Arrange
        when(idempotencyRepository.findById(ID))
                .thenReturn(Optional.of(new IdempotencyEntity(ID, "{\"id\":\"123\"}", null)));

        when(json.fromJson("{\"id\":\"123\"}", OrderResponseDTO.class))
                .thenReturn(orderResponseDTO);

        // Act
        var result = orderService.purchaseShoppingCart(ID, ID.toString());

        // Assert
        assertThat(result).isEqualTo(orderResponseDTO);

        verify(shoppingCartService, never()).findByCustomerId(any());
        verify(orderRepository, never()).save(any());
        verify(orderEventProducer, never()).publishOrderCreated(any());
        verify(orderEventProducer, never()).publishClearCart(any());
    }

    @Test
    void testPurchaseShoppingCartWithoutShippingAddress() {
        // Arrange
        var shippingAddress = shoppingCartEntity.getCustomer().getAddress().get(0);
        shippingAddress.setType(AddressTypeEnum.BILLING);

        when(shoppingCartService.findByCustomerId(ID)).thenReturn(shoppingCartEntity);
        when(orderMapper.toOrderItemsEntity(shoppingCartEntity.getItems())).thenReturn(orderEntity.getOrderItems());
        when(orderMapper.toOrderAddressEntity(shippingAddress)).thenReturn(createOrderAddressEntity());
        when(orderMapper.toResponseDTO(any(OrderEntity.class))).thenReturn(orderResponseDTO);

        // Act
        OrderResponseDTO result = orderService.purchaseShoppingCart(ID, IDEMPOTENCY_KEY);

        // Assert
        assertThat(result).isNotNull().isEqualTo(orderResponseDTO);
        verify(shoppingCartService).findByCustomerId(ID);
        verify(orderRepository).save(any(OrderEntity.class));
        verify(orderEventProducer).publishOrderCreated(any(OrderCreatedEvent.class));
        verify(orderEventProducer).publishClearCart(String.valueOf(ID));
    }

    @Test
    void testPurchaseShoppingCartEmptyCart() {
        // Arrange
        shoppingCartEntity.getItems().clear();

        when(idempotencyRepository.findById(UUID.fromString(TestConstants.IDEMPOTENCY_KEY)))
                .thenReturn(Optional.empty());
        when(shoppingCartService.findByCustomerId(shoppingCartEntity.getCustomer().getId()))
                .thenReturn(shoppingCartEntity);

        // Act & Assert
        assertThatException()
                .isThrownBy(() -> orderService.purchaseShoppingCart(ID, IDEMPOTENCY_KEY))
                .isInstanceOf(EmptyShoppingCartException.class)
                .withMessageContaining(SHOPPING_CART_IS_EMPTY);
        verify(shoppingCartService).findByCustomerId(shoppingCartEntity.getCustomer().getId());
    }

    @Test
    void testUpdateStatus() {
        // Arrange
        String orderIdStr = orderEntity.getId().toString();
        when(orderRepository.findById(orderEntity.getId())).thenReturn(Optional.of(orderEntity));

        // Act
        orderService.updateStatus(orderIdStr, OrderStatusEnum.PAID);

        // Assert
        assertThat(orderEntity.getStatus()).isEqualTo(OrderStatusEnum.PAID);
        verify(orderRepository).save(orderEntity);
    }
}
