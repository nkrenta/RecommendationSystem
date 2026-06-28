CREATE TABLE IF NOT EXISTS users
(
    id
    UUID
    PRIMARY
    KEY,
    username
    VARCHAR
(
    255
) UNIQUE,
    first_name VARCHAR
(
    255
),
    last_name VARCHAR
(
    255
)
    );

CREATE TABLE IF NOT EXISTS products
(
    id
    UUID
    PRIMARY
    KEY,
    type
    VARCHAR
(
    50
) NOT NULL
    );

CREATE TABLE IF NOT EXISTS transactions
(
    id
    UUID
    PRIMARY
    KEY,
    product_id
    UUID
    NOT
    NULL,
    user_id
    UUID
    NOT
    NULL,
    type
    VARCHAR
(
    50
) NOT NULL,
    amount DOUBLE NOT NULL,
    FOREIGN KEY
(
    product_id
) REFERENCES products
(
    id
),
    FOREIGN KEY
(
    user_id
) REFERENCES users
(
    id
)
    );

CREATE TABLE IF NOT EXISTS rule_stats
(
    id
    UUID
    PRIMARY
    KEY,
    rule_id
    UUID
    NOT
    NULL
    UNIQUE,
    count
    BIGINT
    NOT
    NULL
    DEFAULT
    0
);

CREATE TABLE IF NOT EXISTS dynamic_rules
(
    id
    UUID
    PRIMARY
    KEY,
    product_name
    VARCHAR
(
    255
) NOT NULL,
    product_id UUID NOT NULL,
    product_text TEXT NOT NULL
    );

CREATE TABLE IF NOT EXISTS rule_queries
(
    id
    UUID
    PRIMARY
    KEY,
    rule_id
    UUID
    NOT
    NULL,
    query_type
    VARCHAR
(
    255
) NOT NULL,
    negate BOOLEAN NOT NULL,
    FOREIGN KEY
(
    rule_id
) REFERENCES dynamic_rules
(
    id
) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS rule_query_arguments
(
    query_id
    UUID
    NOT
    NULL,
    argument
    VARCHAR
(
    255
),
    FOREIGN KEY
(
    query_id
) REFERENCES rule_queries
(
    id
) ON DELETE CASCADE,
    PRIMARY KEY
(
    query_id,
    argument
)
    );