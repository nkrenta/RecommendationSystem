package ru.skypro.recommendationsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * JPA-сущность, представляющая профиль пользователя рекомендательной системы.
 * <p>
 * Хранится в таблице {@code users} и служит основным контекстом для формирования персонализированных
 * рекомендаций. Все бизнес-сущности (транзакции, срабатывания правил) должны быть строго привязаны
 * к этому идентификатору.
 * </p>
 *
 * <h3>Разделение идентификаторов:</h3>
 * <dl>
 *     <dt>{@code id} (UUID)</dt>
 *         <dd>Внутренний технический идентификатор. Используется во всех внешних ключах
 *             (FK) других таблиц (транзакции, статистика, правила). Не должен показываться пользователю
 *             и не должен использоваться в URL API напрямую (только в теле запроса или заголовках).</dd>
 *
 *     <dt>{@code username}</dt>
 *         <dd>Публичный идентификатор (логин/никнейм). Должен быть уникальным. Может использоваться
 *             в UI для отображения или в админке для поиска, но не должен быть основой для связей
 *             в БД из-за риска изменения имени в будущем.</dd>
 * </dl>
 *
 * <h3>Связь с бизнес-логикой:</h3>
 * <p>
 * Эта сущность является точкой входа для {@link ru.skypro.recommendationsystem.service.RecommendationService}.
 * Сервис принимает {@code UUID userId}, загружает профиль (если нужны имя/фамилия для кастомизации текста),
 * анализирует историю транзакций этого пользователя и применяет к ним {@link ru.skypro.recommendationsystem.entity.DynamicRule}.
 * </p>
 *
 * @see DynamicRule
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * Внутренний уникальный идентификатор пользователя.
     * <p>
     * Используется как первичный ключ в БД и как внешний ключ во всех связанных таблицах
     * (транзакции, история рекомендаций, статистика правил).
     * Тип {@code UUID} выбран для обеспечения глобальной уникальности и возможности
     * шардирования данных в будущем.
     * </p>
     */
    @Id
    private UUID id;

    /**
     * Публичное имя пользователя (логин).
     * <p>
     * Должно быть уникальным в пределах системы. Используется для аутентификации и отображения
     * в интерфейсе. В отличие от {@code id}, может теоретически измениться в будущем
     * (политика переименования), поэтому не используется как основа для жестких связей в БД.
     * </p>
     */
    @Column(name = "username", unique = true)
    private String username;

    /**
     * Имя пользователя для отображения.
     * <p>
     * Необязательное поле. Может использоваться для персонализации текста рекомендации
     * (например, "Иван, у нас есть для вас специальное предложение...").
     * Если имя не указано, система должна использовать дефолтный текст без обращения по имени.
     * </p>
     */
    @Column(name = "first_name")
    private String firstName;

    /**
     * Фамилия пользователя для отображения.
     * <p>
     * Необязательное поле. Используется совместно с {@code firstName} для формирования
     * полного имени в отчетах или персонализированных сообщениях.
     * </p>
     */
    @Column(name = "last_name")
    private String lastName;

    /**
     * Конструктор по умолчанию, необходимый для работы JPA.
     */
    public User() {
    }

    /**
     * Конструктор для создания сущности с полным набором данных.
     *
     * @param id        уникальный идентификатор пользователя
     * @param username  публичное имя (логин)
     * @param firstName имя
     * @param lastName  фамилия
     */
    public User(UUID id, String username, String firstName, String lastName) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        // Сеттер оставлен для совместимости с JPA, но ID обычно генерируется при создании
        // и не должен изменяться в течение жизненного цикла сущности.
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
