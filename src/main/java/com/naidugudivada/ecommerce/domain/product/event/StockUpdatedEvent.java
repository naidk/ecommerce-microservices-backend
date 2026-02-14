package com.naidugudivada.ecommerce.domain.product.event;

import com.naidugudivada.ecommerce.domain.common.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StockUpdatedEvent extends BaseEvent {

    private UUID productId;
    private Integer newQuantity;
}
