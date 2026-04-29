package com.trustchat.dao;

import com.trustchat.model.Message;
import com.trustchat.util.DBUtil;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class MessageDAO {

    private final DBUtil.RowMapper<Message> messageMapper = rs -> {
        Message msg = new Message();
        msg.setMessageId(rs.getInt("message_id"));
        msg.setSenderId(rs.getInt("sender_id"));
        msg.setReceiverId(rs.getInt("receiver_id"));
        msg.setMessageText(rs.getString("message_text"));
        msg.setSentAt(rs.getTimestamp("sent_at", Calendar.getInstance(TimeZone.getDefault())));
        msg.setRead(rs.getBoolean("is_read"));
        msg.setBlocked(rs.getBoolean("is_blocked"));
        msg.setBlockReason(rs.getString("block_reason"));
        
        try {
            msg.setSenderUsername(rs.getString("sender_username"));
            msg.setReceiverUsername(rs.getString("receiver_username"));
        } catch (SQLException ignored) {
            // Fields might not be present in all queries
        }
        return msg;
    };

    public boolean sendMessage(Message message) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, message_text, is_blocked, block_reason) VALUES (?, ?, ?, ?, ?)";
        int id = DBUtil.executeInsertReturnId(sql, message.getSenderId(), message.getReceiverId(), message.getMessageText(), message.isBlocked(), message.getBlockReason());
        
        if (id > 0) {
            message.setMessageId(id);
            return true;
        }
        return false;
    }

    public List<Message> getConversation(int userId1, int userId2) {
        String sql = "SELECT m.*, s.username AS sender_username, r.username AS receiver_username FROM messages m " +
                     "JOIN users s ON m.sender_id = s.user_id JOIN users r ON m.receiver_id = r.user_id " +
                     "WHERE (m.sender_id = ? AND m.receiver_id = ?) OR (m.sender_id = ? AND m.receiver_id = ?) ORDER BY m.sent_at ASC";
                     
        return DBUtil.executeQuery(sql, messageMapper, userId1, userId2, userId2, userId1);
    }

    public void markConversationAsRead(int currentUserId, int otherUserId) {
        String sql = "UPDATE messages SET is_read = TRUE WHERE receiver_id = ? AND sender_id = ? AND is_read = FALSE";
        DBUtil.executeUpdate(sql, currentUserId, otherUserId);
    }

    public int getUnreadCount(int userId) {
        String sql = "SELECT COUNT(*) FROM messages WHERE receiver_id = ? AND is_read = FALSE AND is_blocked = FALSE";
        return DBUtil.queryForInt(sql, userId);
    }

    public Map<Integer, Integer> getUnreadCountPerSender(int currentUserId) {
        Map<Integer, Integer> unreadMap = new HashMap<>();
        String sql = "SELECT sender_id, COUNT(*) AS unread_count FROM messages WHERE receiver_id = ? AND is_read = FALSE AND is_blocked = FALSE GROUP BY sender_id";
        
        // Custom mapper just for this map logic
        DBUtil.executeQuery(sql, rs -> {
            unreadMap.put(rs.getInt("sender_id"), rs.getInt("unread_count"));
            return null; // Return value not used here
        }, currentUserId);
        
        return unreadMap;
    }

    public Message getLastMessage(int userId1, int userId2) {
        String sql = "SELECT m.*, s.username AS sender_username, r.username AS receiver_username FROM messages m " +
                     "JOIN users s ON m.sender_id = s.user_id JOIN users r ON m.receiver_id = r.user_id " +
                     "WHERE ((m.sender_id = ? AND m.receiver_id = ?) OR (m.sender_id = ? AND m.receiver_id = ?)) " +
                     "AND m.is_blocked = FALSE ORDER BY m.sent_at DESC LIMIT 1";
                     
        return DBUtil.queryForObject(sql, messageMapper, userId1, userId2, userId2, userId1);
    }

    public List<Message> getAllBlockedMessages() {
        String sql = "SELECT m.*, s.username AS sender_username, r.username AS receiver_username FROM messages m " +
                     "JOIN users s ON m.sender_id = s.user_id JOIN users r ON m.receiver_id = r.user_id WHERE m.is_blocked = TRUE ORDER BY m.sent_at DESC";
        return DBUtil.executeQuery(sql, messageMapper);
    }

    public List<Message> getReceivedMessages(int userId) {
        String sql = "SELECT m.*, s.username AS sender_username, r.username AS receiver_username FROM messages m " +
                     "JOIN users s ON m.sender_id = s.user_id JOIN users r ON m.receiver_id = r.user_id WHERE m.receiver_id = ? ORDER BY m.sent_at DESC";
        return DBUtil.executeQuery(sql, messageMapper, userId);
    }

    public void markAsRead(int messageId) {
        String sql = "UPDATE messages SET is_read = TRUE WHERE message_id = ?";
        DBUtil.executeUpdate(sql, messageId);
    }

    public Message getMessageById(int messageId) {
        String sql = "SELECT m.*, s.username AS sender_username, r.username AS receiver_username FROM messages m " +
                     "JOIN users s ON m.sender_id = s.user_id JOIN users r ON m.receiver_id = r.user_id WHERE m.message_id = ?";
        return DBUtil.queryForObject(sql, messageMapper, messageId);
    }
}
