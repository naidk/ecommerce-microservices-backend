package com.naidugudivada.ecommerce.domain.product.review.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class DuplicateReviewException extends ResponseStatusException {
    public DuplicateReviewException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
