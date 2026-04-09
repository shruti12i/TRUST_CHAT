package com.trustchat.engine;

import com.trustchat.dao.PolicyDAO;
import com.trustchat.model.Message;
import com.trustchat.model.PolicyRule;
import com.trustchat.model.User;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PolicyEngine {

    private PolicyDAO policyDAO;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public PolicyEngine() {
        this.policyDAO = new PolicyDAO();
    }

    public PolicyCheckResult checkMessage(Message message, User sender, User receiver) {
        List<PolicyRule> activeRules = policyDAO.getActiveRules();

        for (PolicyRule rule : activeRules) {
            String blockReason = null;

            switch (rule.getRuleType()) {
                case "keyword":
                    blockReason = checkKeywordFilter(message.getMessageText(), rule.getRuleValue());
                    break;

                case "time":
                    blockReason = checkTimeRestriction(rule.getRuleValue());
                    break;

                case "user_role":
                    blockReason = checkRoleRestriction(sender, receiver, rule.getRuleValue());
                    break;
            }

            if (blockReason != null) {
                return new PolicyCheckResult(true, blockReason, rule.getRuleName());
            }
        }

        return new PolicyCheckResult(false, null, null);
    }

    private String checkKeywordFilter(String messageText, String blockedWords) {
        if (messageText == null || blockedWords == null) {
            return null;
        }

        String[] keywords = blockedWords.split(",");
        String lowerMessage = messageText.toLowerCase();

        for (String keyword : keywords) {
            keyword = keyword.trim().toLowerCase();
            if (!keyword.isEmpty() && lowerMessage.contains(keyword)) {
                return "Message contains blocked keyword: " + keyword;
            }
        }

        return null;
    }

    private String checkTimeRestriction(String timeRange) {
        if (timeRange == null || !timeRange.contains("-")) {
            return null;
        }

        try {
            String[] times = timeRange.split("-");
            LocalTime startTime = LocalTime.parse(times[0].trim(), TIME_FORMAT);
            LocalTime endTime = LocalTime.parse(times[1].trim(), TIME_FORMAT);
            LocalTime currentTime = LocalTime.now();

            boolean isWithinWindow;
            if (startTime.isBefore(endTime)) {
                isWithinWindow = !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
            } else {
                isWithinWindow = !currentTime.isBefore(startTime) || !currentTime.isAfter(endTime);
            }

            if (!isWithinWindow) {
                return "Messages are only allowed between " + startTime + " and " + endTime;
            }
        } catch (Exception e) {
            System.err.println("Error parsing time restriction: " + e.getMessage());
        }

        return null;
    }

    private String checkRoleRestriction(User sender, User receiver, String ruleValue) {
        if (ruleValue == null) {
            return null;
        }

        try {
            String[] parts = ruleValue.split(":");
            if (parts.length != 2) {
                return null;
            }

            String roleMatch = parts[0].trim();
            String action = parts[1].trim().toLowerCase();

            String[] roles = roleMatch.split("->");
            if (roles.length != 2) {
                return null;
            }

            String senderRole = roles[0].trim().toLowerCase();
            String receiverRole = roles[1].trim().toLowerCase();

            boolean senderMatches = sender.getRole().equalsIgnoreCase(senderRole);
            boolean receiverMatches = receiver.getRole().equalsIgnoreCase(receiverRole);

            if (senderMatches && receiverMatches && "deny".equals(action)) {
                return sender.getRole() + " cannot send messages to " + receiver.getRole();
            }
        } catch (Exception e) {
            System.err.println("Error parsing role restriction: " + e.getMessage());
        }

        return null;
    }

    public static class PolicyCheckResult {
        private final boolean isBlocked;
        private final String blockReason;
        private final String triggeredRule;

        public PolicyCheckResult(boolean isBlocked, String blockReason, String triggeredRule) {
            this.isBlocked = isBlocked;
            this.blockReason = blockReason;
            this.triggeredRule = triggeredRule;
        }

        public boolean isBlocked() {
            return isBlocked;
        }

        public String getBlockReason() {
            return blockReason;
        }

        public String getTriggeredRule() {
            return triggeredRule;
        }
    }
}
