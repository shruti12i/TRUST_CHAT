package com.trustchat.dao;

import com.trustchat.model.PolicyRule;
import com.trustchat.util.DBUtil;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PolicyDAO {

    private final DBUtil.RowMapper<PolicyRule> policyMapper = rs -> {
        PolicyRule rule = new PolicyRule();
        rule.setRuleId(rs.getInt("rule_id"));
        rule.setRuleName(rs.getString("rule_name"));
        rule.setRuleType(rs.getString("rule_type"));
        rule.setRuleValue(rs.getString("rule_value"));
        rule.setActive(rs.getBoolean("is_active"));
        rule.setCreatedBy(rs.getInt("created_by"));
        rule.setCreatedAt(rs.getTimestamp("created_at"));
        return rule;
    };

    public List<PolicyRule> getActiveRules() {
        String sql = "SELECT DISTINCT rule_id, rule_name, rule_type, rule_value, is_active, created_by, created_at FROM policy_rules WHERE is_active = TRUE";
        return DBUtil.executeQuery(sql, policyMapper);
    }

    public List<PolicyRule> getAllRules() {
        String sql = "SELECT DISTINCT rule_id, rule_name, rule_type, rule_value, is_active, created_by, created_at FROM policy_rules ORDER BY created_at DESC";
        return DBUtil.executeQuery(sql, policyMapper);
    }

    public PolicyRule getRuleById(int ruleId) {
        String sql = "SELECT DISTINCT rule_id, rule_name, rule_type, rule_value, is_active, created_by, created_at FROM policy_rules WHERE rule_id = ?";
        return DBUtil.queryForObject(sql, policyMapper, ruleId);
    }

    public boolean addRule(PolicyRule rule) {
        // First check if a duplicate rule exists
        String checkSql = "SELECT COUNT(*) FROM policy_rules WHERE rule_name = ? AND rule_type = ?";
        if (DBUtil.queryForInt(checkSql, rule.getRuleName(), rule.getRuleType()) > 0) {
            return false; // Rule exists
        }

        String sql = "INSERT INTO policy_rules (rule_name, rule_type, rule_value, created_by, is_active) VALUES (?, ?, ?, ?, ?)";
        int newId = DBUtil.executeInsertReturnId(sql, rule.getRuleName(), rule.getRuleType(), rule.getRuleValue(), rule.getCreatedBy(), rule.isActive());
        
        if (newId > 0) {
            rule.setRuleId(newId);
            return true;
        }
        return false;
    }

    public boolean updateRule(PolicyRule rule) {
        String sql = "UPDATE policy_rules SET rule_name = ?, rule_type = ?, rule_value = ?, is_active = ? WHERE rule_id = ?";
        return DBUtil.executeUpdate(sql, rule.getRuleName(), rule.getRuleType(), rule.getRuleValue(), rule.isActive(), rule.getRuleId());
    }

    public boolean deleteRule(int ruleId) {
        String sql = "DELETE FROM policy_rules WHERE rule_id = ?";
        return DBUtil.executeUpdate(sql, ruleId);
    }

    public boolean toggleRuleStatus(int ruleId) {
        String sql = "UPDATE policy_rules SET is_active = NOT is_active WHERE rule_id = ?";
        return DBUtil.executeUpdate(sql, ruleId);
    }
}
