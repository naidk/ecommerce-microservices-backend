package com.naidugudivada.ecommerce.utils;

import com.naidugudivada.ecommerce.domain.order.OrderEntity;
import com.naidugudivada.ecommerce.domain.order.OrderStatusEnum;
import com.naidugudivada.ecommerce.domain.order.dto.OrderResponseDTO;
import com.naidugudivada.ecommerce.domain.order.orderaddress.OrderAddressEntity;
import com.naidugudivada.ecommerce.domain.order.orderaddress.dto.OrderAddressResponseDTO;
import com.naidugudivada.ecommerce.domain.order.orderitem.OrderItemEntity;
import com.naidugudivada.ecommerce.domain.order.orderitem.dto.OrderItemResponseDTO;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.naidugudivada.ecommerce.utils.CustomerTestUtils.createCustomerEntity;
import static com.naidugudivada.ecommerce.utils.ProductTestUtils.createProductEntity;
import static com.naidugudivada.ecommerce.utils.ProductTestUtils.createProductResponseDTO;
import static com.naidugudivada.ecommerce.utils.TestConstants.ID;

@UtilityClass
public class OrderTestUtils {

    public static OrderAddressEntity createOrderAddressEntity() {
        return OrderAddressEntity.builder()
                .id(ID)
                .street("123 Test St")
                .number("100")
                .neighborhood("Test Neighborhood")
                .city("Test City")
                .state("Test State")
                .country("Test Country")
                .zipCode("12345")
                .additionalInfo("Apartment 1A")
                .build();
    }

    public static OrderItemEntity createOrderItemEntity() {
        return OrderItemEntity.builder()
                .id(ID)
                .product(createProductEntity())
                .quantity(2)
                .priceAtPurchase(BigDecimal.valueOf(50))
                .totalPrice(BigDecimal.valueOf(100))
                .build();
    }

    public static OrderEntity createOrderEntity() {
        return OrderEntity.builder()
                .id(ID)
                .customer(createCustomerEntity())
                .orderItems(List.of(createOrderItemEntity()))
                .shippingAddress(createOrderAddressEntity())
                .totalPrice(BigDecimal.valueOf(100))
                .status(OrderStatusEnum.PENDING)
                .build();
    }

    public static OrderAddressResponseDTO createOrderAddressResponseDTO() {
        var address = createOrderAddressEntity();
        return new OrderAddressResponseDTO(
                address.getId(),
                address.getStreet(),
                address.getNumber(),
                address.getNeighborhood(),
                address.getCity(),
                address.getState(),
                address.getCountry(),
                address.getZipCode(),
                address.getAdditionalInfo(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public static OrderItemResponseDTO createOrderItemResponseDTO() {
        var item = createOrderItemEntity();
        return new OrderItemResponseDTO(
                item.getId(),
                createProductResponseDTO(),
                item.getQuantity(),
                item.getPriceAtPurchase(),
                item.getTotalPrice(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public static OrderResponseDTO createOrderResponseDTO() {
        return new OrderResponseDTO(
                ID,
                CustomerTestUtils.createCustomerResponseDTO(),
                List.of(createOrderItemResponseDTO()),
                createOrderAddressResponseDTO(),
                BigDecimal.valueOf(100),
                OrderStatusEnum.PENDING.name(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
