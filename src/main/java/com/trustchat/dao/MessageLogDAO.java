package com.trustchat.dao;

import com.trustchat.util.DBUtil;

import java.sql.*;

public class MessageLogDAO {

    public void log(int userId, String action, String details) {
        String sql = "INSERT INTO message_logs (user_id, action, details) VALUES (?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, action);
            pstmt.setString(3, details);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Warning: could not write to message_logs — " + e.getMessage());
        }
    }
}
