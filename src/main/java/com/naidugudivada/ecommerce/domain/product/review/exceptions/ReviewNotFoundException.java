package com.naidugudivada.ecommerce.domain.product.review.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ReviewNotFoundException extends ResponseStatusException {
    public ReviewNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
