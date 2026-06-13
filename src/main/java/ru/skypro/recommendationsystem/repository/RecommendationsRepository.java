package ru.skypro.recommendationsystem.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class RecommendationsRepository {

    private final JdbcTemplate jdbcTemplate;

    public RecommendationsRepository(@Qualifier("recommendationsJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int getRandomTransactionAmount(UUID user) {
        Integer result = jdbcTemplate.queryForObject(
                "SELECT amount FROM transactions t WHERE t.user_id = ? ORDER BY RAND() LIMIT 1",
                Integer.class,
                user);
        return result != null ? result : 0;
    }

     //создаём метод для проверки наличия продукта определённого типа у пользователя
    public boolean hasProductType (UUID userId, String productType){
        Boolean result = jdbcTemplate.queryForObject(
                "SELECT COUNT(*)>0 FROM transactions t "+       //проверка наличия хотя бы одной записи
                        "JOIN products p ON t.product_id = p.id "+  //объединение таблицы транзакций и продуктов
                        "WHERE t.user_id = ? AND p.type = ?",       //фильтрация получившейся таблицы по id пользователя и типу продукта
                Boolean.class,
                userId,
                productType);
                return result;
    }

    //создаём метод для получения суммарного пополнения по типу продукта
    public Double getTotalDepositsByProductType(UUID userId, String productType) {
        Double result = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(t.amount), 0) FROM transactions t " + // Возвращает 0, если транзакций нет.
                "JOIN products p ON t.product_id = p.id " +                    //объединение таблицы транзакций и продуктов
                "WHERE t.user_id = ? AND p.type = ? AND t.type = 'DEPOSIT'",   // Фильтрует таблицу только по пополнению Deposit
                Double.class,
                userId,
                productType);
        return result;
    }

     //создаём метод для получения суммарных трат по типу продукта
    public Double getTotalWithdrawalsByProductType(UUID userId, String productType) {
        Double result = jdbcTemplate.queryForObject(
         "SELECT COALESCE(SUM(t.amount), 0) FROM transactions t " +       // Возвращает 0, если транзакций нет.
                "JOIN products p ON t.product_id = p.id " +                   //объединение таблицы транзакций и продуктов
                "WHERE t.user_id = ? AND p.type = ? AND t.type = 'WITHDRAW'",// Фильтрует таблицу только по тратам
                Double.class,
                userId,
                productType);
        return result;
    }
}

