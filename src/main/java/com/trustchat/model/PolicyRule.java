package com.trustchat.model;

import java.sql.Timestamp;

public class PolicyRule {
    private int ruleId;
    private String ruleName;
    private String ruleType;
    private String ruleValue;
    private boolean isActive;
    private int createdBy;
    private Timestamp createdAt;

    public PolicyRule() {
    }

    public PolicyRule(String ruleName, String ruleType, String ruleValue) {
        this.ruleName = ruleName;
        this.ruleType = ruleType;
        this.ruleValue = ruleValue;
        this.isActive = true;
    }

    public int getRuleId() {
        return ruleId;
    }

    public void setRuleId(int ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getRuleValue() {
        return ruleValue;
    }

    public void setRuleValue(String ruleValue) {
        this.ruleValue = ruleValue;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "PolicyRule{" +
                "ruleId=" + ruleId +
                ", ruleName='" + ruleName + '\'' +
                ", ruleType='" + ruleType + '\'' +
                ", ruleValue='" + ruleValue + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
