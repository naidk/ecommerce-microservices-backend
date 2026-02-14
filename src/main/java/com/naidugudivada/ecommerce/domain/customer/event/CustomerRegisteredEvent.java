package com.naidugudivada.ecommerce.domain.customer.event;

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
public class CustomerRegisteredEvent extends BaseEvent {

    private UUID customerId;
    private String email;
    private String name;
}
