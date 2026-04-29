<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.trustchat.model.*, java.util.*, java.text.SimpleDateFormat" %>
<%
    User currentUser = (User) session.getAttribute("user");
    List<User> users = (List<User>) request.getAttribute("users");
    Integer unreadCount = (Integer) request.getAttribute("unreadCount");
    Map<Integer, Integer> unreadPerSender = (Map<Integer, Integer>) request.getAttribute("unreadPerSender");
    Map<Integer, Message> lastMessages = (Map<Integer, Message>) request.getAttribute("lastMessages");
    
    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm");
    sdf.setTimeZone(TimeZone.getDefault());
    
    Integer offlineDelivered = (Integer) session.getAttribute("offlineDelivered");
    if (offlineDelivered != null) session.removeAttribute("offlineDelivered");
    
    String dashboardError = (String) session.getAttribute("dashboardError");
    if (dashboardError != null) session.removeAttribute("dashboardError");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - TRUST_CHAT</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .unread-badge {
            display: inline-block;
            background: #dc3545;
            color: white;
            font-size: 11px;
            font-weight: bold;
            padding: 2px 7px;
            border-radius: 10px;
            margin-left: 6px;
            vertical-align: middle;
        }
        .last-message-preview {
            font-size: 13px;
            color: #888;
            margin-top: 6px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            max-width: 200px;
        }
        .last-message-preview .preview-time {
            font-size: 11px;
            color: #aaa;
            margin-left: 6px;
        }
    </style>
</head>
<body>
    <div class="dashboard-layout">
        <div class="sidebar">
            <div class="sidebar-header">
                <h2>🔐 TRUST_CHAT</h2>
            </div>
            
            <div class="user-info">
                <p><strong>Welcome,</strong></p>
                <p><%= currentUser.getUsername() %></p>
                <span class="role"><%= currentUser.getRole().toUpperCase() %></span>
            </div>
            
            <ul class="sidebar-menu">
                <li><a href="${pageContext.request.contextPath}/dashboard" class="active">📬 Users</a></li>
                <% if(currentUser.isAdmin()){ %>
                    <li><a href="${pageContext.request.contextPath}/admin">⚙️ Admin Panel</a></li>
                <% } %>
                <li><a href="${pageContext.request.contextPath}/logout">🚪 Logout</a></li>
            </ul>
        </div>
        
        <div class="main-content">
            <div class="container">
                <h1>👥 Available Users</h1>
                
                <% if (dashboardError != null) { %>
                    <div class="alert alert-error">🚫 <%= dashboardError %></div>
                <% } %>
                
                <% if (offlineDelivered != null && offlineDelivered > 0) { %>
                    <div class="alert alert-success">
                        📬 <%= offlineDelivered %> offline message<%= offlineDelivered > 1 ? "s" : "" %> delivered.
                    </div>
                <% } %>
                
                <p style="color: #666; margin-top: 10px;">
                    Select a user to start chatting 
                    <% if (unreadCount != null && unreadCount > 0) { %>
                        <span class="unread-badge"><%= unreadCount %> unread</span>
                    <% } %>
                </p>
                
                <% if (users != null && !users.isEmpty()) { %>
                    <div class="user-list">
                        <% for (User user : users) {
                            boolean isStudentChat = "student".equalsIgnoreCase(currentUser.getRole()) && "student".equalsIgnoreCase(user.getRole());
                            int userUnread = (!isStudentChat && unreadPerSender != null && unreadPerSender.containsKey(user.getUserId())) ? unreadPerSender.get(user.getUserId()) : 0;
                            Message lastMsg = (lastMessages != null) ? lastMessages.get(user.getUserId()) : null;
                            
                            String preview = "";
                            if (lastMsg != null) {
                                String txt = lastMsg.getMessageText();
                                preview = txt.length() > 40 ? txt.substring(0, 40) + "…" : txt;
                                preview = preview.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                            }
                        %>
                        <div class="user-card">
                            <h3>
                                <%= user.getUsername() %> 
                                <% if (userUnread > 0) { %>
                                    <span class="unread-badge"><%= userUnread %> new</span>
                                <% } %>
                            </h3>
                            <span class="role"><%= user.getRole() %></span>
                            
                            <% if (lastMsg != null) { %>
                                <p class="last-message-preview">
                                    <%= lastMsg.getSenderId() == currentUser.getUserId() ? "You: " : "" %>
                                    <%= preview %>
                                    <span class="preview-time"><%= sdf.format(lastMsg.getSentAt()) %></span>
                                </p>
                            <% } else { %>
                                <p class="last-message-preview" style="font-style: italic;">No messages yet</p>
                            <% } %>
                            
                            <a href="${pageContext.request.contextPath}/chat?receiverId=<%= user.getUserId() %>" class="btn btn-small">💬 Chat</a>
                        </div>
                        <% } %>
                    </div>
                <% } else { %>
                    <div class="no-messages">
                        <p>No other users found.</p>
                    </div>
                <% } %>
            </div>
        </div>
    </div>
</body>
</html>
