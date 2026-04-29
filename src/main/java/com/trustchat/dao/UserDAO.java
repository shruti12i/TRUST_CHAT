package com.trustchat.dao;

import com.trustchat.model.User;
import com.trustchat.util.DBUtil;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDAO {

    // Reusable RowMapper to map a database row to a User object
    private final DBUtil.RowMapper<User> userMapper = rs -> {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getString("role"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setLastLogin(rs.getTimestamp("last_login"));
        user.setActive(rs.getBoolean("is_active"));
        return user;
    };

    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND is_active = TRUE";
        User user = DBUtil.queryForObject(sql, userMapper, username, password);
        
        if (user != null) {
            updateLastLogin(user.getUserId());
        }
        return user;
    }

    public boolean register(User user) {
        String role = user.getRole();
        if (!"student".equalsIgnoreCase(role) && !"teacher".equalsIgnoreCase(role)) {
            return false; // Invalid role
        }

        String sql = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?)";
        return DBUtil.executeUpdate(sql, user.getUsername(), user.getPassword(), user.getEmail(), role.toLowerCase());
    }

    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        return DBUtil.queryForObject(sql, userMapper, userId);
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        return DBUtil.queryForObject(sql, userMapper, username);
    }

    public List<User> getAllUsersExcept(int currentUserId) {
        String sql = "SELECT * FROM users WHERE user_id != ? AND is_active = TRUE ORDER BY username";
        return DBUtil.executeQuery(sql, userMapper, currentUserId);
    }

    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        return DBUtil.executeQuery(sql, userMapper);
    }

    private void updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = NOW() WHERE user_id = ?";
        DBUtil.executeUpdate(sql, userId);
    }

    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        return DBUtil.queryForInt(sql, username) > 0;
    }

    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        return DBUtil.queryForInt(sql, email) > 0;
    }
}
