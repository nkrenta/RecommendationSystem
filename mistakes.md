# Ошибки проекта RecommendationSystem

---

## Ошибка 1: Тестовая база не знает про таблицы динамических правил

### Что происходит
При запуске интеграционных тестов приложение падает с ошибкой:
```
Table "DYNAMIC_RULES" not found
```
### Почему это происходит
В тестовом профиле Liquibase отключён (`spring.liquibase.enabled: false` в `application-test.yml`), а таблицы для динамических правил (`dynamic_rules`, `rule_queries`, `rule_query_arguments`) создаются именно Liquibase-скриптами. Файл `schema.sql`, который используется для тестов, содержит только три таблицы: `users`, `products`, `transactions`. Когда `DynamicRuleRepository.findAllWithQueries()` пытается обратиться к таблице `dynamic_rules` — её просто нет.

### Где искать
- `src/test/resources/schema.sql` — здесь не хватает определений таблиц
- `src/main/resources/liquibase/scripts/` — здесь есть нужные CREATE TABLE, но в тестовом профиле они не выполняются

### Варианты решения

**Вариант А (рекомендуемый):** Добавить CREATE TABLE в `schema.sql`

Добавить в конец `src/test/resources/schema.sql`:
```sql
CREATE TABLE dynamic_rules (
    id UUID PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    product_id UUID NOT NULL,
    product_text TEXT NOT NULL
);

CREATE TABLE rule_queries (
    id UUID PRIMARY KEY,
    rule_id UUID NOT NULL,
    query_type VARCHAR(255) NOT NULL,
    negate BOOLEAN NOT NULL,
    FOREIGN KEY (rule_id) REFERENCES dynamic_rules(id)
);

CREATE TABLE rule_query_arguments (
    query_id UUID NOT NULL,
    argument VARCHAR(255),
    FOREIGN KEY (query_id) REFERENCES rule_queries(id)
);
```

Плюсы: просто и понятно. Минусы: нужно дублировать структуру, которая уже есть в Liquibase.

**Вариант Б:** Включить Liquibase для тестового профиля

В `application-test.yml` поставить `spring.liquibase.enabled: true` и настроить его на H2. Но Liquibase скрипты написаны для PostgreSQL, и могут быть несовместимости с H2.

**Вариант В:** Использовать `CREATE TABLE IF NOT EXISTS` в schema.sql

Добавить `IF NOT EXISTS` ко всем CREATE TABLE, чтобы избежать ошибок при повторном запуске.

---

## Ошибка 2: Тест contextLoads падает без тестового профиля

### Что происходит
Тест `RecommendationDTOSystemApplicationTests.contextLoads()` падает при запуске:
```
IllegalStateException: Failed to load ApplicationContext
...
Table "USERS" already exists
```

### Почему это происходит
Класс `RecommendationDTOSystemApplicationTests` использует `@SpringBootTest`, но **не указывает** `@ActiveProfiles("test")`. Без этого Spring загружает основной профиль, где:
1. Активируется `DataSourceConfig` (он помечен `@Profile("!test")`), который настраивает DataSource на PostgreSQL
2. H2-таблицы создаются, но потом `schema.sql` пытается создать их ещё раз → ошибка "уже существует"

### Где искать
- `src/test/java/ru/skypro/recommendationsystem/RecommendationDTOSystemApplicationTests.java`

### Варианты решения

**Вариант А (рекомендуемый):** Добавить `@ActiveProfiles("test")`

```java
@SpringBootTest
@ActiveProfiles("test")
class RecommendationDTOSystemApplicationTests {
    @Test
    void contextLoads() {
    }
}
```

Это заставит Spring использовать `TestDataSourceConfig` (H2 in-memory) вместо `DataSourceConfig` (PostgreSQL).

**Вариант Б:** Удалить этот тест вообще

Если он не нужен — можно просто удалить файл. Но обычно `contextLoads()` полезен для проверки, что Spring-контекст собирается без ошибок.

---

## Ошибка 3: DynamicRuleService принимает зависимости, которые не использует

### Что происходит
Компиляция проходит, но при запуске Spring создаёт лишние бины, а код выглядит запутанным.

### Почему это происходит
Конструктор `DynamicRuleService` принимает три параметра:
```java
public DynamicRuleService(DynamicRuleRepository repository,
                          RecommendationsRepository recommendationsRepository,
                          @Qualifier("recommendationsJdbcTemplate") JdbcTemplate jdbcTemplate) {
    this.repository = repository;
}
```
Параметры `recommendationsRepository` и `jdbcTemplate` **принимаются, но не сохраняются** в поля класса. Это значит:
- Spring вынужден создавать и инжектить эти бины, хотя они не нужны
- Другие разработчики могут подумать, что эти зависимости используются
- При запуске в тестовом профиле Spring будет пытаться найти `JdbcTemplate` с квалификатором `recommendationsJdbcTemplate`, что может вызвать дополнительные проблемы

### Где искать
- `src/main/java/ru/skypro/recommendationsystem/service/DynamicRuleService.java`

### Варианты решения

**Вариант А (рекомендуемый):** Убрать лишние параметры из конструктора

```java
public DynamicRuleService(DynamicRuleRepository repository) {
    this.repository = repository;
}
```

Также удалить неиспользуемые импорты `RecommendationsRepository`, `Qualifier`, `JdbcTemplate`.

**Вариант Б:** Если зависимости нужны для будущего — сохранить их как поля

```java
private final DynamicRuleRepository repository;
private final RecommendationsRepository recommendationsRepository;
private final JdbcTemplate jdbcTemplate;

public DynamicRuleService(DynamicRuleRepository repository,
                          RecommendationsRepository recommendationsRepository,
                          @Qualifier("recommendationsJdbcTemplate") JdbcTemplate jdbcTemplate) {
    this.repository = repository;
    this.recommendationsRepository = recommendationsRepository;
    this.jdbcTemplate = jdbcTemplate;
}
```

---

## Ошибка 4: DynamicRuleSet возвращает только первую найденную рекомендацию

### Что происходит
Если у пользователя выполняются условия **нескольких** динамических правил, вернётся только **одна** рекомендация — та, что идёт первой в базе данных. Остальные игнорируются.

### Почему это происходит
В методе `DynamicRuleSet.checkRecommendation()` используется `findFirst()`:
```java
return dynamicRuleRepository.findAllWithQueries().stream()
        .map(rule -> checkRuleForUser(userId, rule))
        .filter(Optional::isPresent)
        .findFirst()           // ← останавливается на первом совпадении
        .orElse(Optional.empty());
```

Статические правила (`Invest500RuleImpl`, `TopSavingRuleImpl`, `UsualCreditRuleImpl`) возвращают по одной рекомендации каждое, но их **все** добавляют в общий список в `RecommendationService`. А вот `DynamicRuleSet` — один `@Component`, который отвечает за **все** динамические правила, и возвращает только одно.

### Где искать
- `src/main/java/ru/skypro/recommendationsystem/service/dynamicRuleSet/DynamicRuleSet.java`

### Варианты решения

**Вариант А (рекомендуемый):** Изменить интерфейс `RecommendationRuleSet`

Сейчас интерфейс возвращает `Optional<RecommendationDTO>` (одна рекомендация). Для динамических правил нужен список. Можно изменить интерфейс на:
```java
public interface RecommendationRuleSet {
    List<RecommendationDTO> checkRecommendation(UUID userId);
}
```

И обновить все реализации (`Invest500RuleImpl`, `TopSavingRuleImpl`, `UsualCreditRuleImpl`, `DynamicRuleSet`). Статические правила будут возвращать список из одного элемента или пустой список.

**Вариант Б:** Убрать `DynamicRuleSet` из списка `RecommendationRuleSet` и обработать динамические правила отдельно в `RecommendationService`

```java
@Service
public class RecommendationService {
    private final List<RecommendationRuleSet> recommendationRules;
    private final DynamicRuleSet dynamicRuleSet;  // отдельная зависимость

    public RecommendationResponse getRecommendationsForUser(UUID userId) {
        List<RecommendationDTO> staticRecs = recommendationRules.stream()
                .flatMap(rule -> rule.checkRecommendation(userId).stream())
                .collect(Collectors.toList());

        List<RecommendationDTO> dynamicRecs = dynamicRuleSet.findAllForUser(userId);

        List<RecommendationDTO> all = new ArrayList<>(dynamicRecs);
        all.addAll(staticRecs);
        return new RecommendationResponse(userId, all);
    }
}
```

И добавить в `DynamicRuleSet` метод `findAllForUser(UUID userId)`, который возвращает `List<RecommendationDTO>`.

**Вариант В:** Внутри `DynamicRuleSet` собирать все совпадения, а интерфейс оставить как есть

```java
@Override
public Optional<List<RecommendationDTO>> checkRecommendation(UUID userId) {
    List<RecommendationDTO> results = dynamicRuleRepository.findAllWithQueries().stream()
            .map(rule -> checkRuleForUser(userId, rule))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    return results.isEmpty() ? Optional.empty() : Optional.of(results);
}
```

Но это ломает контракт интерфейса и потребует изменений в `RecommendationService`.

---

## Ошибка 5: RecommendationService не объединяет рекомендации от динамических и статических правил правильно

### Что происходит
Связана с ошибкой 4. `RecommendationService.getRecommendationsForUser()` собирает рекомендации из списка `List<RecommendationRuleSet>`, куда входит и `DynamicRuleSet`. Поскольку `DynamicRuleSet` возвращает только одну рекомендацию (findFirst), пользователь может получить неполный набор рекомендаций.

### Почему это происходит
`RecommendationService`:
```java
List<RecommendationDTO> recommendations = recommendationRules.stream()
        .map(rule -> rule.checkRecommendation(userId))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
```

Каждый `RecommendationRuleSet` отдаёт по одному `Optional<RecommendationDTO>`. Для статических правил это нормально (каждое правило — отдельный компонент, возвращает 0 или 1 рекомендацию). Но `DynamicRuleSet` — один компонент, который проверяет **все** динамические правила, и должен возвращать **все** совпадения, а не одно.

### Где искать
- `src/main/java/ru/skypro/recommendationsystem/service/RecommendationService.java`

### Варианты решения
Смотрите варианты решения ошибки 4. После исправления ошибки 4 эта ошибка устранится автоматически. Ключевой момент: интерфейс `RecommendationRuleSet` должен либо:
- Возвращать `List<RecommendationDTO>` вместо `Optional<RecommendationDTO>`, либо
- `DynamicRuleSet` должен быть обработан отдельно от статических правил

---

## Порядок исправления

1. **Сначала ошибка 1** — без таблиц в тестовой базеичего не заработает
2. **Потом ошибка 2** — добавить `@ActiveProfiles("test")` в `contextLoads`
3. **Потом ошибки 4 и 5** — исправить `DynamicRuleSet` и `RecommendationService`
4. **Последней ошибка 3** — убрать лишние зависимости из `DynamicRuleService`
