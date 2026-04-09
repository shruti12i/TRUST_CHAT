<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register - TRUST_CHAT</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="auth-container">
        <div class="auth-card">
            <h1>🔐 TRUST_CHAT</h1>
            <h3 style="text-align: center; color: #666; margin-bottom: 20px;">Create Account</h3>

            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-error">
                    <%= request.getAttribute("error") %>
                </div>
            <% } %>

            <form method="post" action="${pageContext.request.contextPath}/register">
                <div class="form-group">
                    <label for="username">Username</label>
                    <input type="text" id="username" name="username"
                           value="${username}" required>
                </div>

                <div class="form-group">
                    <label for="email">Email</label>
                    <input type="email" id="email" name="email"
                           value="${email}" required>
                </div>

                <div class="form-group">
                    <label for="role">Role</label>
                    <select id="role" name="role" required>
                        <option value="">-- Select Role --</option>
                        <option value="student" ${role == 'student' ? 'selected' : ''}>Student</option>
                        <option value="teacher" ${role == 'teacher' ? 'selected' : ''}>Teacher</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password"
                           minlength="6" required>
                </div>

                <div class="form-group">
                    <label for="confirmPassword">Confirm Password</label>
                    <input type="password" id="confirmPassword" name="confirmPassword" required>
                </div>

                <button type="submit" class="btn">Register</button>
            </form>

            <div class="auth-links">
                <p>Already have an account?
                    <a href="${pageContext.request.contextPath}/login">Login here</a>
                </p>
            </div>
        </div>
    </div>
</body>
</html>
