# Checkout: Работоспособность эндпоинтов

Дата проверки: 2026-06-24

## Результаты

| # | Эндпоинт | Метод | Тело запроса | Ожидаемый код | Фактический код | Статус |
|---|---|---|---|---|---|---|
| 1 | `/recommendation/9d2df4a9-0085-4838-b8af-d8b46659cb62` | GET | — | 200 | 200 | OK (пустой список рекомендаций) |
| 2 | `/recommendation/e809075f-1752-411a-8e0c-de3bae23e1b9` | GET | — | 200 | 200 | OK (1 рекомендация: Top Saving) |
| 3 | `/recommendation/invalid-uuid` | GET | — | 400 | 400 | OK (Bad Request) |
| 4 | `/rule/createRule` | POST | `{"productName":"Test Product","productId":"147f6a0f-3b91-413b-ab99-87f081d60d5a","productText":"Test description","queries":[]}` | 200 | 200 | OK (правило создано, возвращён UUID) |
| 5 | `/rule/getAllRules` | GET | — | 200 | 200 | OK (список динамических правил) |
| 6 | `/rule/deleteRule/a5fa305a-0433-4eae-b887-a67635634a68` | DELETE | — | 204 | 204 | OK (правило удалено) |
| 7 | `/rule/getAllRules` | GET | — | 200 | 200 | OK (список пуст после удаления) |
| 8 | `/swagger-ui/index.html` | GET | — | 200 | 200 | OK |
| 9 | `/v3/api-docs` | GET | — | 200 | 200 | OK |
| 10 | `/actuator` | GET | — | 200 | 200 | OK |
| 11 | `/h2-console` | GET | — | 200 | 200 | OK |

## Выводы

- Все эндпоинты отвечают корректными кодами ответов
- CRUD для динамических правил работает: создание, получение, удаление
- Эндпоинт рекомендаций работает, но пользователь `9d2df4a9...` получает пустой список (ожидались 3 рекомендации: Top Saving, Usual Credit, INVEST500)
- Swagger UI и OpenAPI документация доступны
- H2 Console и Actuator доступны
