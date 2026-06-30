package ru.skypro.recommendationsystem.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RuleStatsDTO {
    @JsonProperty("rule_id")
    private String ruleId;
    private long count;

    public RuleStatsDTO() {
    }

    public RuleStatsDTO(String ruleId, long count) {
        this.ruleId = ruleId;
        this.count = count;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
