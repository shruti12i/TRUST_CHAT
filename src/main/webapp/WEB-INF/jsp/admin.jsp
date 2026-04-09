<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.trustchat.model.User" %>
<%@ page import="com.trustchat.model.PolicyRule" %>
<%@ page import="com.trustchat.model.Message" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.TimeZone" %>
<%
    User currentUser              = (User)         session.getAttribute("user");
    List<PolicyRule> rules        = (List<PolicyRule>) request.getAttribute("rules");
    List<User>       users        = (List<User>)   request.getAttribute("users");
    List<Message>    blockedMsgs  = (List<Message>) request.getAttribute("blockedMessages");
    SimpleDateFormat sdf          = new SimpleDateFormat("MMM dd, yyyy HH:mm");
    sdf.setTimeZone(TimeZone.getDefault());
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Panel - TRUST_CHAT</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .blocked-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
            font-size: 14px;
        }
        .blocked-table th {
            background: #dc3545;
            color: white;
            padding: 10px 12px;
            text-align: left;
        }
        .blocked-table td {
            padding: 10px 12px;
            border-bottom: 1px solid #eee;
            vertical-align: top;
        }
        .blocked-table tr:hover td {
            background: #fff5f5;
        }
        .blocked-text {
            max-width: 250px;
            word-break: break-word;
        }
        .block-reason-badge {
            display: inline-block;
            background: #ffeaea;
            color: #c0392b;
            font-size: 12px;
            padding: 2px 8px;
            border-radius: 4px;
        }
    </style>
</head>
<body>
    <div class="dashboard-layout">
        <div class="sidebar">
            <div class="sidebar-header"><h2>⚙️ Admin</h2></div>
            <div class="user-info">
                <p><strong>Admin:</strong></p>
                <p><%= currentUser.getUsername() %></p>
            </div>
            <ul class="sidebar-menu">
                <li><a href="${pageContext.request.contextPath}/dashboard">📬 Dashboard</a></li>
                <li><a href="${pageContext.request.contextPath}/admin" class="active">📋 Policy Rules</a></li>
                <li><a href="${pageContext.request.contextPath}/logout">🚪 Logout</a></li>
            </ul>
        </div>

        <div class="main-content">
            <div class="admin-header">
                <h1>🛡️ Policy Engine Management</h1>
                <p style="color: #666; margin-top: 10px;">
                    The 3 predefined message filtering rules for the chat system
                </p>
            </div>

            <% if (request.getAttribute("success") != null) { %>
                <div class="alert alert-success"><%= request.getAttribute("success") %></div>
            <% } %>
            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-error"><%= request.getAttribute("error") %></div>
            <% } %>

            <h2 style="margin-bottom: 20px;">📋 Policy Rules</h2>

            <% if (rules != null && !rules.isEmpty()) { %>
                <table class="rules-table">
                    <thead>
                        <tr>
                            <th>Rule Name</th>
                            <th>Type</th>
                            <th>Value</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (PolicyRule rule : rules) { %>
                            <tr>
                                <td><strong><%= rule.getRuleName() %></strong></td>
                                <td><%= rule.getRuleType() %></td>
                                <td><%= rule.getRuleValue() %></td>
                                <td>
                                    <span class="status-badge <%= rule.isActive() ? "status-active" : "status-inactive" %>">
                                        <%= rule.isActive() ? "Active" : "Inactive" %>
                                    </span>
                                </td>
                                <td>
                                    <form method="post" action="${pageContext.request.contextPath}/admin" style="display: inline;">
                                        <input type="hidden" name="action" value="toggleRule">
                                        <input type="hidden" name="ruleId" value="<%= rule.getRuleId() %>">
                                        <button type="submit" class="btn btn-small btn-secondary">
                                            <%= rule.isActive() ? "Disable" : "Enable" %>
                                        </button>
                                    </form>
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            <% } else { %>
                <p style="color: #666;">No policy rules configured yet.</p>
            <% } %>

            <div style="margin-top: 40px;">
                <h2 style="margin-bottom: 10px;">🚫 Blocked Messages Log</h2>
                <p style="color: #666; margin-bottom: 16px;">
                    All messages blocked by the policy engine. Only visible to admin.
                </p>

                <% if (blockedMsgs != null && !blockedMsgs.isEmpty()) { %>
                    <table class="blocked-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Sent At</th>
                                <th>From</th>
                                <th>To</th>
                                <th>Message</th>
                                <th>Block Reason</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% int rowNum = 1;
                               for (Message msg : blockedMsgs) {
                                   String safeText = msg.getMessageText()
                                       .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                            %>
                                <tr>
                                    <td><%= rowNum++ %></td>
                                    <td style="white-space: nowrap;"><%= sdf.format(msg.getSentAt()) %></td>
                                    <td><strong><%= msg.getSenderUsername() %></strong></td>
                                    <td><strong><%= msg.getReceiverUsername() %></strong></td>
                                    <td class="blocked-text"><%= safeText %></td>
                                    <td><span class="block-reason-badge"><%= msg.getBlockReason() %></span></td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                <% } else { %>
                    <p style="color: #666; font-style: italic;">No blocked messages yet.</p>
                <% } %>
            </div>

            <div style="margin-top: 40px;">
                <h2 style="margin-bottom: 16px;">👥 Registered Users</h2>
                <% if (users != null && !users.isEmpty()) { %>
                    <table class="rules-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Username</th>
                                <th>Email</th>
                                <th>Role</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (User u : users) { %>
                                <tr>
                                    <td><%= u.getUserId() %></td>
                                    <td><strong><%= u.getUsername() %></strong></td>
                                    <td><%= u.getEmail() %></td>
                                    <td><%= u.getRole() %></td>
                                    <td>
                                        <span class="status-badge <%= u.isActive() ? "status-active" : "status-inactive" %>">
                                            <%= u.isActive() ? "Active" : "Inactive" %>
                                        </span>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                <% } %>
            </div>

        </div>
    </div>
</body>
</html>
