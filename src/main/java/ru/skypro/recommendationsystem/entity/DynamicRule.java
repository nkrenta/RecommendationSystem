package ru.skypro.recommendationsystem.entity;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dynamic_rules")
public class DynamicRule implements Persistable<UUID> {

    @Id
    private UUID id;

    @Transient
    private boolean isNew = true;
    @Column(name = "product_name", nullable = false)
    private String productName;
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    @Column(name = "product_text", nullable = false)
    private String productText;
    @OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "rule_id", nullable = false)
    private List<RuleQuery> queries;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getProductText() {
        return productText;
    }

    public void setProductText(String productText) {
        this.productText = productText;
    }

    public List<RuleQuery> getQueries() {
        return queries;
    }

    public void setQueries(List<RuleQuery> queries) {
        this.queries = queries;
    }
}
