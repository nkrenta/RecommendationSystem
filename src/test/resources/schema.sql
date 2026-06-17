CREATE TABLE users (
    id UUID PRIMARY KEY
);

CREATE TABLE products (
    id UUID PRIMARY KEY,
    type VARCHAR(255) NOT NULL
);

CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    user_id UUID NOT NULL,
    type VARCHAR(255) NOT NULL,
    amount INTEGER NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);