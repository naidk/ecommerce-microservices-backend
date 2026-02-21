CREATE TABLE promotion (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(10, 2) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

ALTER TABLE shopping_cart
ADD COLUMN promotion_id UUID,
ADD COLUMN discount_amount DECIMAL(10, 2) DEFAULT 0.00,
ADD CONSTRAINT fk_shopping_cart_promotion FOREIGN KEY (promotion_id) REFERENCES promotion(id);
