package com.trustchat.dao;

import com.trustchat.util.DBUtil;

public class OfflineMessageDAO {

    public boolean queueOfflineMessage(int senderId, int receiverId, String messageText) {
        String sql = "INSERT INTO offline_messages (sender_id, receiver_id, message_text) VALUES (?, ?, ?)";
        return DBUtil.executeUpdate(sql, senderId, receiverId, messageText);
    }

    public int deliverOfflineMessages(int receiverId) {
        // executeUpdate returns true/false based on if rows were changed,
        // but the original code returned the number of rows.
        // For simplicity, we can do a query to find the count first, then update them.
        
        String countSql = "SELECT COUNT(*) FROM offline_messages WHERE receiver_id = ? AND delivered_at IS NULL";
        int pending = DBUtil.queryForInt(countSql, receiverId);
        
        if (pending > 0) {
            String updateSql = "UPDATE offline_messages SET delivered_at = NOW() WHERE receiver_id = ? AND delivered_at IS NULL";
            DBUtil.executeUpdate(updateSql, receiverId);
        }
        
        return pending;
    }

    public int getPendingCount(int receiverId) {
        String sql = "SELECT COUNT(*) FROM offline_messages WHERE receiver_id = ? AND delivered_at IS NULL";
        return DBUtil.queryForInt(sql, receiverId);
    }
}
