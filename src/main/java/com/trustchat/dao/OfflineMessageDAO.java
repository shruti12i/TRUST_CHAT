package com.trustchat.dao;

import com.trustchat.model.Message;
import com.trustchat.util.DBUtil;

import java.sql.*;

public class OfflineMessageDAO {

    public boolean queueOfflineMessage(int senderId, int receiverId, String messageText) {
        String sql = "INSERT INTO offline_messages (sender_id, receiver_id, message_text) " +
                     "VALUES (?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, senderId);
            pstmt.setInt(2, receiverId);
            pstmt.setString(3, messageText);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int deliverOfflineMessages(int receiverId) {
        String sql = "UPDATE offline_messages " +
                     "SET delivered_at = NOW() " +
                     "WHERE receiver_id = ? AND delivered_at IS NULL";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, receiverId);
            return pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getPendingCount(int receiverId) {
        String sql = "SELECT COUNT(*) FROM offline_messages " +
                     "WHERE receiver_id = ? AND delivered_at IS NULL";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, receiverId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
