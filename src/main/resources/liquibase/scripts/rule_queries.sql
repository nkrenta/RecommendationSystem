-- liquibase formatted sql

-- changeset yar:2
CREATE TABLE rule_queries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    query_type VARCHAR(255) NOT NULL,
    negate BOOLEAN NOT NULL,
    rule_id UUID NOT NULL,
    CONSTRAINT fk_rule_queries_rule FOREIGN KEY (rule_id) REFERENCES dynamic_rules(id)
);