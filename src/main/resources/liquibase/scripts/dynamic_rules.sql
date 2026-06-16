-- liquibase formatted sql

-- changeset mkorolkov:1
CREATE TABLE dynamic_rules (
    id BIGSERIAL PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    product_id UUID NOT NULL,
    product_text TEXT NOT NULL
);