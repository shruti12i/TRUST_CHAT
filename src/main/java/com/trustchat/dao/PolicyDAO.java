package com.trustchat.dao;

import com.trustchat.model.PolicyRule;
import com.trustchat.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PolicyDAO {

    public List<PolicyRule> getActiveRules() {
        List<PolicyRule> rules = new ArrayList<>();
        String sql = "SELECT DISTINCT rule_id, rule_name, rule_type, rule_value, is_active, created_by, created_at FROM policy_rules WHERE is_active = TRUE";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                rules.add(mapResultSetToPolicyRule(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rules;
    }

    public List<PolicyRule> getAllRules() {
        List<PolicyRule> rules = new ArrayList<>();
        String sql = "SELECT DISTINCT rule_id, rule_name, rule_type, rule_value, is_active, created_by, created_at FROM policy_rules ORDER BY created_at DESC";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                rules.add(mapResultSetToPolicyRule(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rules;
    }

    public PolicyRule getRuleById(int ruleId) {
        String sql = "SELECT DISTINCT rule_id, rule_name, rule_type, rule_value, is_active, created_by, created_at FROM policy_rules WHERE rule_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ruleId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToPolicyRule(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addRule(PolicyRule rule) {
        String checkSql = "SELECT COUNT(*) FROM policy_rules WHERE rule_name = ? AND rule_type = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, rule.getRuleName());
            checkStmt.setString(2, rule.getRuleType());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        String sql = "INSERT INTO policy_rules (rule_name, rule_type, rule_value, created_by, is_active) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, rule.getRuleName());
            pstmt.setString(2, rule.getRuleType());
            pstmt.setString(3, rule.getRuleValue());
            pstmt.setInt(4, rule.getCreatedBy());
            pstmt.setBoolean(5, rule.isActive());

            int result = pstmt.executeUpdate();

            if (result > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    rule.setRuleId(generatedKeys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateRule(PolicyRule rule) {
        String sql = "UPDATE policy_rules SET rule_name = ?, rule_type = ?, rule_value = ?, is_active = ? WHERE rule_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, rule.getRuleName());
            pstmt.setString(2, rule.getRuleType());
            pstmt.setString(3, rule.getRuleValue());
            pstmt.setBoolean(4, rule.isActive());
            pstmt.setInt(5, rule.getRuleId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteRule(int ruleId) {
        String sql = "DELETE FROM policy_rules WHERE rule_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ruleId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean toggleRuleStatus(int ruleId) {
        String sql = "UPDATE policy_rules SET is_active = NOT is_active WHERE rule_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ruleId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private PolicyRule mapResultSetToPolicyRule(ResultSet rs) throws SQLException {
        PolicyRule rule = new PolicyRule();
        rule.setRuleId(rs.getInt("rule_id"));
        rule.setRuleName(rs.getString("rule_name"));
        rule.setRuleType(rs.getString("rule_type"));
        rule.setRuleValue(rs.getString("rule_value"));
        rule.setActive(rs.getBoolean("is_active"));
        rule.setCreatedBy(rs.getInt("created_by"));
        rule.setCreatedAt(rs.getTimestamp("created_at"));
        return rule;
    }
}
