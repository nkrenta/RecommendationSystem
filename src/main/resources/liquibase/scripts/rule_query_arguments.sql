-- liquibase formatted sql

-- changeset mkorolkov:3
CREATE TABLE rule_query_arguments (
    query_id BIGINT NOT NULL,
    argument VARCHAR(255),
    CONSTRAINT fk_rule_query_arguments_query FOREIGN KEY (query_id) REFERENCES rule_queries(id)
);