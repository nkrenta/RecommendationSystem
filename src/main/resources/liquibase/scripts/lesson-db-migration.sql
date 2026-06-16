-- liquibase formatted sql
databaseChangeLog:
  - changeSet:
      id: 1
      author: your-name
      changes:
        - createTable:
            tableName: dynamic_rules
            columns:
              - column:
                  name: id
                  type: UUID
                  autoIncrement: false
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: product_name
                  type: VARCHAR(255)
                  constraints:
                    nullable: false
              - column:
                  name: product_id
                  type: VARCHAR(50)
                  constraints:
                    nullable: false
              - column:
                  name: product_text
                  type: TEXT

  - changeSet:
      id: 2
      author: your-name
      changes:
        - createTable:
            tableName: rule_queries
            columns:
              - column:
                  name: id
                  type: UUID
                  autoIncrement: false
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: query_type
                  type: VARCHAR(100)
              - column:
                  name: negate
                  type: BOOLEAN
              - column:
                  name: rule_id
                  type: UUID

  - changeSet:
      id: 3
      author: your-name
      changes:
        - createTable:
            tableName: rule_query_arguments
            columns:
              - column:
                  name: query_id
                  type: UUID
                  constraints:
                    nullable: false
              - column:
                  name: argument
                  type: VARCHAR(255)
