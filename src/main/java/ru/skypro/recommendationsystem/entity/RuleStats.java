package ru.skypro.recommendationsystem.entity;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * JPA-сущность, хранящая агрегированную статистику срабатываний динамического правила.
 * <p>
 * Находится в таблице {@code rule_stats} и служит источником данных для аналитических дашбордов
 * (например, эндпоинт {@link ru.skypro.recommendationsystem.controller.RuleStatsController}).
 * </p>
 *
 * <h3>Бизнес-ценность метрики:</h3>
 * Поле {@code count} отражает количество успешных применений правила к пользователям.
 * Эта статистика критически важна для:
 * <ul>
 *     <li><b>Оценки релевантности:</b> Правила с низким count могут быть неактуальными или иметь
 *         слишком жесткие условия. Их стоит пересмотреть или отключить.</li>
 *
 *     <li><b>Выявления хитов:</b> Правила с аномально высоким count могут указывать на "универсальное"
 *         предложение, которое стоит вынести на главную страницу или сделать статическим.</li>
 *
 *     <li><b>A/B-тестирования:</b> При сравнении версий правил (v1 vs v2) статистика позволяет
 *         объективно выбрать победителя на основе количества срабатываний.</li>
 * </ul>
 *
 * <h3>Особенности реализации JPA:</h3>
 * <dl>
 *     <dt>Связь с правилом ({@code ruleId})</dt>
 *         <dd>Поле {@code ruleId} хранит ссылку на {@link DynamicRule}. Ограничение
 *             {@code unique = true} гарантирует, что для каждого правила существует ровно
 *             одна запись статистики. Это упрощает обновление счетчика через {@code INSERT} или
 *             {@code UPDATE} без риска дублирования.</dd>
 *
 *     <dt>Генерация ID</dt>
 *         <dd>Собственный {@code id} сущности генерируется БД ({@code GenerationType.UUID}).
 *             Он нужен для внутреннего управления записями, но бизнес-логика опирается на
 *             связь по {@code ruleId}.</dd>
 * </dl>
 *
 * @see DynamicRule
 */
@Entity
@Table(name = "rule_stats")
public class RuleStats {

    /**
     * Внутренний уникальный идентификатор записи статистики.
     * <p>
     * Генерируется базой данных автоматически. Используется для технических операций
     * (удаление, выборка по ID), но не является ключевым для бизнес-логики.
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false, unique = true)
    private DynamicRule dynamicRule;

    /**
     * Агрегированный счетчик срабатываний правила.
     * <p>
     * Хранит количество раз, когда правило было успешно применено к пользователям
     * и рекомендация была показана (или принята).
     * Тип {@code long} выбран для поддержки высоких нагрузок и долгосрочного накопления данных
     * без переполнения.</p>
     */
    @Column(name = "count", nullable = false)
    private long count;

    /**
     * Конструктор по умолчанию, необходимый для работы JPA.
     */
    public RuleStats() {
    }

    public RuleStats(DynamicRule dynamicRule, long count) {
        this.dynamicRule = dynamicRule;
        this.count = count;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        // Сеттер оставлен для совместимости с JPA, но ID должен генерироваться БД.
        this.id = id;
    }

    public DynamicRule getDynamicRule() {
        return dynamicRule;
    }

    public void setDynamicRule(DynamicRule dynamicRule) {
        this.dynamicRule = dynamicRule;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
