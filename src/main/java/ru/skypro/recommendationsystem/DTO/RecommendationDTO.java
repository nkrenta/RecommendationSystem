package ru.skypro.recommendationsystem.DTO;

import java.util.Objects;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) для передачи данных о персональной рекомендации.
 * <p>
 * Используется как контракт ответа для эндпоинта {@code GET /recommendation/{userId}}
 * в {@link ru.skypro.recommendationsystem.controller.RecommendationController}.
 * </p>
 *
 * <h3>Назначение полей:</h3>
 * <ul>
 *     <li>{@code id} — уникальный идентификатор рекомендации. Позволяет фронтенду
 *         отслеживать конкретный элемент списка, кэшировать его или отправлять телеметрию.</li>
 *
 *     <li>{@code name} — отображаемое название продукта/предложения. Должно быть кратким
 *         и понятным для пользователя (например, "Вклад под 15%").</li>
 *
 *     <li>{@code text} — детальный текст рекомендации. Содержит описание условий, преимуществ
 *         или призыв к действию. Может содержать форматирование (HTML/Markdown), если это
 *         согласовано с фронтендом.</li>
 * </ul>
 *
 * <h3>Особенности использования:</h3>
 * <ul>
 *     <li><b>Сериализация/Десериализация:</b> Класс совместим со стандартными JSON-библиотеками
 *         (Jackson, Gson) благодаря наличию конструктора по умолчанию и сеттеров.</li>
 *
 *     <li><b>Сравнение объектов:</b> Реализованы методы {@link #equals(Object)} и {@link #hashCode()},
 *         учитывающие все три поля. Это позволяет корректно использовать объекты в {@code Set}
 *         или как ключи в {@code Map} (например, для дедупликации рекомендаций в сервисном слое).</li>
 *
 *     <li><b>Отладка:</b> Переопределен метод {@link #toString()}, выводящий все поля в читаемом формате.</li>
 * </ul>
 *
 * @see ru.skypro.recommendationsystem.controller.RecommendationController
 * @see RecommendationResponse
 */
public class RecommendationDTO {

    /**
     * Уникальный идентификатор рекомендации.
     * <p>
     * Генерируется на стороне сервиса. Не должен изменяться после создания.
     * </p>
     */
    private UUID id;

    /**
     * Название рекомендации для отображения пользователю.
     * <p>
     * Должно быть непустой строкой. Длина ограничена бизнес-требованиями UI (обычно до 50-100 символов).
     * </p>
     */
    private String name;

    /**
     * Текст рекомендации (описание, условия, призыв к действию).
     * <p>
     * Может содержать расширенное форматирование. Длина может быть значительной,
     * но на фронтенде должна быть предусмотрена обрезка (truncate) при превышении лимита экрана.
     * </p>
     */
    private String text;

    /**
     * Конструктор по умолчанию, необходимый для десериализации JSON.
     */
    public RecommendationDTO() {
    }

    /**
     * Конструктор для создания DTO с инициализированными полями.
     *
     * @param id   уникальный идентификатор
     * @param name название рекомендации
     * @param text текст рекомендации
     */
    public RecommendationDTO(UUID id, String name, String text) {
        this.id = id;
        this.name = name;
        this.text = text;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecommendationDTO that = (RecommendationDTO) o;
        // Сравнение по всем полям: ID, Name и Text.
        // Это важно, так как две рекомендации могут иметь одинаковый ID, но разный текст (ошибка данных),
        // и они должны считаться разными объектами.
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(text, that.text);
    }

    @Override

    public int hashCode() {
        // Хэш-код вычисляется на основе всех полей для согласованности с equals().
        return Objects.hash(id, name, text);
    }

    @Override
    public String toString() {
        return "RecommendationDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
