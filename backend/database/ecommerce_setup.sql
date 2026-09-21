-- Run this script while connected to the ecommerce database.
-- Example:
--   createdb -U postgres ecommerce
--   psql -U postgres -d ecommerce -f database/ecommerce_setup.sql

BEGIN;

CREATE TABLE IF NOT EXISTS app_users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(16) NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price NUMERIC(19, 2) NOT NULL CHECK (price > 0),
    stock_quantity INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    total_amount NUMERIC(19, 2) NOT NULL CHECK (total_amount >= 0),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_product
        FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE INDEX IF NOT EXISTS idx_orders_product_id
    ON orders (product_id);

CREATE INDEX IF NOT EXISTS idx_orders_status
    ON orders (status);

INSERT INTO products (name, price, stock_quantity)
SELECT 'Demo product', 99.99, 10
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE name = 'Demo product'
);

COMMIT;

-- Verify the created schema:
-- SELECT * FROM products;
-- SELECT * FROM orders;
