package ru.skypro.recommendationsystem.DTO;

import ru.skypro.recommendationsystem.entity.DynamicRule;

import java.util.List;

public class DynamicRuleListResponse {
    private List<DynamicRule> data;

    public DynamicRuleListResponse() {
    }

    public DynamicRuleListResponse(List<DynamicRule> data) {
        this.data = data;
    }

    public List<DynamicRule> getData() {
        return data;
    }

    public void setData(List<DynamicRule> data) {
        this.data = data;
    }
}
