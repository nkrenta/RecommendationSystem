package ru.skypro.recommendationsystem.entity;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rule_queries")
public class RuleQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(name = "query_type", nullable = false)
    private String query;

    @ElementCollection
    @CollectionTable(name = "rule_query_arguments", joinColumns = @JoinColumn(name = "query_id"))
    @Column(name = "argument")
    private List<String> arguments;

    @Column(name = "negate", nullable = false)
    private boolean negate;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public boolean isNegate() {
        return negate;
    }

    public void setNegate(boolean negate) {
        this.negate = negate;
    }
}
