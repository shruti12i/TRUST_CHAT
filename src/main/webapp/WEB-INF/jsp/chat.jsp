<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.trustchat.model.User" %>
<%@ page import="com.trustchat.model.Message" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.TimeZone" %>
<%
    User currentUser   = (User)         session.getAttribute("user");
    User receiver      = (User)         request.getAttribute("receiver");
    List<Message> messages = (List<Message>) request.getAttribute("messages");
    List<User>    users    = (List<User>)    request.getAttribute("users");

    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm");
    sdf.setTimeZone(TimeZone.getDefault());
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chat with <%= receiver != null ? receiver.getUsername() : "User" %> - TRUST_CHAT</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="chat-layout">

        <div class="chat-sidebar">
            <div class="chat-sidebar-header">
                <a href="${pageContext.request.contextPath}/dashboard"
                   style="text-decoration: none; color: #667eea;">
                    ← Back to Dashboard
                </a>
            </div>
            <ul class="chat-user-list">
                <% if (users != null) {
                       for (User user : users) {
                           boolean isStudentChat = "student".equalsIgnoreCase(currentUser.getRole()) &&
                                                   "student".equalsIgnoreCase(user.getRole());
                           if (isStudentChat) continue;
                %>
                    <li class="chat-user-item <%= user.getUserId() == receiver.getUserId() ? "active" : "" %>">
                        <a href="${pageContext.request.contextPath}/chat?receiverId=<%= user.getUserId() %>"
                           style="text-decoration: none; color: inherit;">
                            <span class="username"><%= user.getUsername() %></span>
                            <span class="role-badge"><%= user.getRole() %></span>
                        </a>
                    </li>
                <% }} %>
            </ul>
        </div>

        <div class="chat-main">
            <div class="chat-header">
                <h2>💬 Chat with <%= receiver.getUsername() %></h2>
                <span class="role-badge"><%= receiver.getRole() %></span>
            </div>

            <% if (request.getAttribute("error") != null) { %>
                <div style="padding: 12px 16px; margin: 10px; background: #f8d7da;
                            color: #721c24; border-radius: 6px; border-left: 4px solid #dc3545;">
                    🚫 <%= request.getAttribute("error") %>
                </div>
            <% } %>

            <div class="chat-messages" id="chatMessages">
                <% if (messages != null && !messages.isEmpty()) {
                       for (Message msg : messages) {
                           boolean isSent = msg.getSenderId() == currentUser.getUserId();
                           boolean isAdmin = "admin".equalsIgnoreCase(currentUser.getRole());

                           if (msg.isBlocked() && !isAdmin) {
                               continue;
                           }

                           String safeText = msg.getMessageText()
                               .replace("&",  "&amp;")
                               .replace("<",  "&lt;")
                               .replace(">",  "&gt;")
                               .replace("\"", "&quot;")
                               .replace("'",  "&#x27;");
                %>
                    <div class="message <%= isSent ? "sent" : "received" %>">
                        <div class="message-bubble" style="<%= msg.isBlocked() ? "opacity: 0.6; border: 1px dashed #dc3545;" : "" %>">
                            <%= safeText %>
                            <% if (msg.isBlocked()) { %>
                                <span style="display: block; font-size: 11px; color: #dc3545; margin-top: 6px; font-weight: bold;">
                                    🚫 BLOCKED — From: <%= msg.getSenderUsername() %> → To: <%= msg.getReceiverUsername() %>
                                </span>
                                <span style="display: block; font-size: 11px; color: #999; margin-top: 2px;">
                                    Reason: <%= msg.getBlockReason() %>
                                </span>
                            <% } %>
                        </div>
                        <div class="message-time">
                            <%= sdf.format(msg.getSentAt()) %>
                        </div>
                    </div>

                <% }} else { %>
                    <div class="no-messages">
                        <p>No messages yet. Start the conversation!</p>
                    </div>
                <% } %>
            </div>

            <div class="chat-input-area">
                <form method="post" action="${pageContext.request.contextPath}/chat"
                      class="chat-input-form">
                    <input type="hidden" name="receiverId" value="<%= receiver.getUserId() %>">
                    <input type="text" name="message"
                           placeholder="Type your message..."
                           required autofocus maxlength="2000">
                    <button type="submit">Send</button>
                </form>
            </div>
        </div>
    </div>

    <script>
        window.onload = function() {
            var chatMessages = document.getElementById('chatMessages');
            chatMessages.scrollTop = chatMessages.scrollHeight;
        };
    </script>
</body>
</html>
