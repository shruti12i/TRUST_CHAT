package com.trustchat.dao;

import com.trustchat.util.DBUtil;

public class MessageLogDAO {

    public void log(int userId, String action, String details) {
        String sql = "INSERT INTO message_logs (user_id, action, details) VALUES (?, ?, ?)";
        DBUtil.executeUpdate(sql, userId, action, details);
    }
}
