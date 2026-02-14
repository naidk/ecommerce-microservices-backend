package com.naidugudivada.ecommerce.domain.shoppingcart.shoppingcartitem;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShoppingCartItemService {

    private final ShoppingCartItemRepository repository;

    @Transactional
    public void save(ShoppingCartItemEntity item) {
        repository.save(item);
    }

    @Transactional
    public void delete(ShoppingCartItemEntity item) {
        repository.delete(item);
    }
}
