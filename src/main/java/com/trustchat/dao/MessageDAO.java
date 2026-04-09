package com.trustchat.dao;

import com.trustchat.model.Message;
import com.trustchat.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class MessageDAO {

    public boolean sendMessage(Message message) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, message_text, is_blocked, block_reason) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, message.getSenderId());
            pstmt.setInt(2, message.getReceiverId());
            pstmt.setString(3, message.getMessageText());
            pstmt.setBoolean(4, message.isBlocked());
            pstmt.setString(5, message.getBlockReason());

            int result = pstmt.executeUpdate();
            if (result > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) message.setMessageId(generatedKeys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Message> getConversation(int userId1, int userId2) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, s.username AS sender_username, r.username AS receiver_username " +
                     "FROM messages m " +
                     "JOIN users s ON m.sender_id = s.user_id " +
                     "JOIN users r ON m.receiver_id = r.user_id " +
                     "WHERE (m.sender_id = ? AND m.receiver_id = ?) " +
                     "OR    (m.sender_id = ? AND m.receiver_id = ?) " +
                     "ORDER BY m.sent_at ASC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId1); pstmt.setInt(2, userId2);
            pstmt.setInt(3, userId2); pstmt.setInt(4, userId1);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) messages.add(mapResultSetToMessage(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public void markConversationAsRead(int currentUserId, int otherUserId) {
        String sql = "UPDATE messages SET is_read = TRUE " +
                     "WHERE receiver_id = ? AND sender_id = ? AND is_read = FALSE";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUserId);
            pstmt.setInt(2, otherUserId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getUnreadCount(int userId) {
        String sql = "SELECT COUNT(*) FROM messages " +
                     "WHERE receiver_id = ? AND is_read = FALSE AND is_blocked = FALSE";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Map<Integer, Integer> getUnreadCountPerSender(int currentUserId) {
        Map<Integer, Integer> unreadMap = new HashMap<>();
        String sql = "SELECT sender_id, COUNT(*) AS unread_count " +
                     "FROM messages " +
                     "WHERE receiver_id = ? AND is_read = FALSE AND is_blocked = FALSE " +
                     "GROUP BY sender_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                unreadMap.put(rs.getInt("sender_id"), rs.getInt("unread_count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return unreadMap;
    }

    public Message getLastMessage(int userId1, int userId2) {
        String sql = "SELECT m.*, s.username AS sender_username, r.username AS receiver_username " +
                     "FROM messages m " +
                     "JOIN users s ON m.sender_id = s.user_id " +
                     "JOIN users r ON m.receiver_id = r.user_id " +
                     "WHERE ((m.sender_id = ? AND m.receiver_id = ?) " +
                     "OR    (m.sender_id = ? AND m.receiver_id = ?)) " +
                     "AND m.is_blocked = FALSE " +
                     "ORDER BY m.sent_at DESC LIMIT 1";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId1); pstmt.setInt(2, userId2);
            pstmt.setInt(3, userId2); pstmt.setInt(4, userId1);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapResultSetToMessage(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Message> getAllBlockedMessages() {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, s.username AS sender_username, r.username AS receiver_username " +
                     "FROM messages m " +
                     "JOIN users s ON m.sender_id = s.user_id " +
                     "JOIN users r ON m.receiver_id = r.user_id " +
                     "WHERE m.is_blocked = TRUE " +
                     "ORDER BY m.sent_at DESC";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) messages.add(mapResultSetToMessage(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public List<Message> getReceivedMessages(int userId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, s.username AS sender_username, r.username AS receiver_username " +
                     "FROM messages m " +
                     "JOIN users s ON m.sender_id = s.user_id " +
                     "JOIN users r ON m.receiver_id = r.user_id " +
                     "WHERE m.receiver_id = ? ORDER BY m.sent_at DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) messages.add(mapResultSetToMessage(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public void markAsRead(int messageId) {
        String sql = "UPDATE messages SET is_read = TRUE WHERE message_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, messageId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Message getMessageById(int messageId) {
        String sql = "SELECT m.*, s.username AS sender_username, r.username AS receiver_username " +
                     "FROM messages m " +
                     "JOIN users s ON m.sender_id = s.user_id " +
                     "JOIN users r ON m.receiver_id = r.user_id " +
                     "WHERE m.message_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, messageId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapResultSetToMessage(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setMessageId(rs.getInt("message_id"));
        message.setSenderId(rs.getInt("sender_id"));
        message.setReceiverId(rs.getInt("receiver_id"));
        message.setMessageText(rs.getString("message_text"));
        message.setSentAt(rs.getTimestamp("sent_at", Calendar.getInstance(TimeZone.getDefault())));
        message.setRead(rs.getBoolean("is_read"));
        message.setBlocked(rs.getBoolean("is_blocked"));
        message.setBlockReason(rs.getString("block_reason"));
        try {
            message.setSenderUsername(rs.getString("sender_username"));
            message.setReceiverUsername(rs.getString("receiver_username"));
        } catch (SQLException e) {
        }
        return message;
    }
}
