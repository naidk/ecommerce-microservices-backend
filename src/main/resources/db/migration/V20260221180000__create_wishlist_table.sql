CREATE TABLE wishlist (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_wishlist_customer FOREIGN KEY (customer_id) REFERENCES customer (id) ON DELETE CASCADE
);

CREATE TABLE wishlist_item (
    id UUID PRIMARY KEY,
    wishlist_id UUID NOT NULL,
    product_id UUID NOT NULL,
    added_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_wishlist_item_wishlist FOREIGN KEY (wishlist_id) REFERENCES wishlist (id) ON DELETE CASCADE,
    CONSTRAINT fk_wishlist_item_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE,
    CONSTRAINT uk_wishlist_product UNIQUE (wishlist_id, product_id)
);
