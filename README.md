# Recommendation System

Сервис рекомендаций для пользователей банка. Предоставляет персонализированные рекомендации через REST API и
телеграм-бота, поддерживает динамические правила, собирает статистику их срабатываний и предоставляет возможности
управления сервисом (например, сброс кеша и получение информации о сервисе).

## Документация

- [Требования](https://github.com/nkrenta/RecommendationSystem/wiki/Requirements)
- [Архитектура](https://github.com/nkrenta/RecommendationSystem/wiki/Architecture)
- [Отслеживание требований](https://github.com/nkrenta/RecommendationSystem/wiki/Requirements_tracking)
- [API-документация](https://github.com/nkrenta/RecommendationSystem/wiki/API)
- [Инструкция по развертыванию](https://github.com/nkrenta/RecommendationSystem/wiki/Deployment)

## Технологии

- Java 17, Spring Boot
- PostgreSQL (правила), H2 (транзакции)
- Liquibase (миграции БД)
- Spring Cache (кэширование)
- OpenAPI (API-документация)
- Telegram Bot API
- JUnit (тесты)

## Quick Start

Подробная инструкция по развертыванию доступна
в [Deployment.md](https://github.com/nkrenta/RecommendationSystem/wiki/Deployment).