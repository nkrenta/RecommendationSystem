-- liquibase formatted sql

-- changeset mkorolkov:2
CREATE TABLE rule_queries (
    id BIGSERIAL PRIMARY KEY,
    query_type VARCHAR(255) NOT NULL,
    negate BOOLEAN NOT NULL,
    rule_id BIGINT NOT NULL,
    CONSTRAINT fk_rule_queries_rule FOREIGN KEY (rule_id) REFERENCES dynamic_rules(id)
);