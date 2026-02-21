package com.naidugudivada.ecommerce.domain.wishlist.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class WishlistException extends RuntimeException {
    public WishlistException(String message) {
        super(message);
    }
}
