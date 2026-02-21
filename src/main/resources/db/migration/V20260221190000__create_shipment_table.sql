CREATE TABLE shipment (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE,
    tracking_number VARCHAR(100) NOT NULL UNIQUE,
    carrier VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    estimated_delivery_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_shipment_order FOREIGN KEY (order_id) REFERENCES order_table(id) ON DELETE CASCADE
);
