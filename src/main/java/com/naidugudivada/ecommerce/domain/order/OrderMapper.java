package com.naidugudivada.ecommerce.domain.order;

import com.naidugudivada.ecommerce.domain.address.AddressEntity;
import com.naidugudivada.ecommerce.domain.order.dto.OrderResponseDTO;
import com.naidugudivada.ecommerce.domain.order.orderaddress.OrderAddressEntity;
import com.naidugudivada.ecommerce.domain.order.orderaddress.dto.OrderAddressResponseDTO;
import com.naidugudivada.ecommerce.domain.order.orderitem.OrderItemEntity;
import com.naidugudivada.ecommerce.domain.order.orderitem.dto.OrderItemResponseDTO;
import java.util.List;

import com.naidugudivada.ecommerce.domain.shoppingcart.shoppingcartitem.ShoppingCartItemEntity;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OrderMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "customer", source = "customer")
    @Mapping(target = "orderItems", source = "orderItems")
    @Mapping(target = "shippingAddress", source = "shippingAddress")
    @Mapping(target = "status", expression = "java(order.getStatus().name())")
    OrderResponseDTO toResponseDTO(OrderEntity order);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "product", source = "product")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "priceAtPurchase", source = "priceAtPurchase")
    @Mapping(target = "totalPrice", expression = "java(item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity())))")
    OrderItemResponseDTO toResponseDTO(OrderItemEntity item);

    @Mapping(target = "id", source = "id")
    OrderAddressResponseDTO toResponseDTO(OrderAddressEntity address);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "priceAtPurchase", source = "priceAtAddedTime")
    @Mapping(target = "totalPrice", expression = "java(item.getPriceAtAddedTime().multiply(BigDecimal.valueOf(item.getQuantity())))")
    OrderItemEntity toOrderItemEntity(ShoppingCartItemEntity item);

    List<OrderItemEntity> toOrderItemsEntity(List<ShoppingCartItemEntity> items);

    @Mapping(target = "id", ignore = true)
    OrderAddressEntity toOrderAddressEntity(AddressEntity address);
}
