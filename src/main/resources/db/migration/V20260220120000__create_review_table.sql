CREATE TABLE IF NOT EXISTS product_review (
  id UUID PRIMARY KEY,
  product_id UUID NOT NULL,
  customer_id UUID NOT NULL,
  rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
  title VARCHAR(255),
  comment TEXT,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
  
  CONSTRAINT fk_review_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
  CONSTRAINT fk_review_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE,
  CONSTRAINT uk_product_customer_review UNIQUE (product_id, customer_id)
);

CREATE INDEX idx_review_product ON product_review(product_id);
