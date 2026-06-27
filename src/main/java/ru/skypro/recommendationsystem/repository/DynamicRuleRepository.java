package ru.skypro.recommendationsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.skypro.recommendationsystem.entity.DynamicRule;

import java.util.List;
import java.util.UUID;

@Repository
public interface DynamicRuleRepository extends JpaRepository<DynamicRule, UUID> {

    // Этот запрос принудительно загружает коллекцию queries через JOIN FETCH
    @Query("SELECT d FROM DynamicRule d LEFT JOIN FETCH d.queries")
    List<DynamicRule> findAllWithQueries();
}