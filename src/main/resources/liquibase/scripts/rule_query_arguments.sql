-- liquibase formatted sql

-- changeset yar:2
CREATE TABLE rule_query_arguments
(
    query_id UUID NOT NULL,
    argument VARCHAR(255),
    CONSTRAINT fk_rule_query_arguments_query FOREIGN KEY (query_id) REFERENCES rule_queries (id)
);