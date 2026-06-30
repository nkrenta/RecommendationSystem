-- liquibase formatted sql

-- changeset EAKok:3
CREATE TABLE rule_stats (
    id UUID PRIMARY KEY,                -- <-- ЭТОГО НЕ ХВАТАЛО
    rule_id UUID NOT NULL UNIQUE,
    count BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (rule_id) REFERENCES dynamic_rules(id)
);