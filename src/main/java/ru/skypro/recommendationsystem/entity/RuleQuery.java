package ru.skypro.recommendationsystem.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "rule_queries")
public class RuleQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "query_type", nullable = false)
    private String query;

    @ElementCollection
    @CollectionTable(name = "rule_query_arguments", joinColumns = @JoinColumn(name = "query_id"))
    @Column(name = "argument")
    private List<String> arguments;

    @Column(name = "negate", nullable = false)
    private boolean negate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public List<String> getArguments() { return arguments; }
    public void setArguments(List<String> arguments) { this.arguments = arguments; }
    public boolean isNegate() { return negate; }
    public void setNegate(boolean negate) { this.negate = negate; }
}
