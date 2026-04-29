<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Error - TRUST_CHAT</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="auth-container">
        <div class="auth-card">
            <h1>⚠️ Something went wrong</h1>
            <p style="color: #666; margin-top: 16px;">
                Error <%= request.getAttribute("javax.servlet.error.status_code") %> — 
                <%= request.getAttribute("javax.servlet.error.message") %>
            </p>
            <a href="${pageContext.request.contextPath}/dashboard" class="btn" style="margin-top: 20px; display: inline-block;">
                ← Go to Dashboard
            </a>
        </div>
    </div>
</body>
</html>
