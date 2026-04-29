package com.trustchat.servlet;

import com.trustchat.dao.UserDAO;
import com.trustchat.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String email = request.getParameter("email");
        String role = request.getParameter("role");

        if (isAnyEmpty(username, password, email, role)) {
            handleError(request, response, "All fields are required", username, email, role);
            return;
        }

        String roleLower = role.trim().toLowerCase();
        if (!roleLower.equals("student") && !roleLower.equals("teacher")) {
            handleError(request, response, "Invalid role. Only Student or Teacher can register.", username, email, "");
            return;
        }

        if (!password.equals(confirmPassword)) {
            handleError(request, response, "Passwords do not match", username, email, role);
            return;
        }

        if (password.length() < 6) {
            handleError(request, response, "Password must be at least 6 characters", username, email, role);
            return;
        }

        if (userDAO.usernameExists(username.trim())) {
            handleError(request, response, "Username already exists", null, email, role);
            return;
        }

        if (userDAO.emailExists(email.trim())) {
            handleError(request, response, "Email already registered", username, null, role);
            return;
        }

        User newUser = new User(username.trim(), password, email.trim(), roleLower);

        if (userDAO.register(newUser)) {
            request.setAttribute("success", "Registration successful! Please login.");
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
        } else {
            handleError(request, response, "Registration failed. Please try again.", username, email, role);
        }
    }

    private boolean isAnyEmpty(String... fields) {
        for (String field : fields) {
            if (field == null || field.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void handleError(HttpServletRequest request, HttpServletResponse response, String errorMessage, 
                             String username, String email, String role) throws ServletException, IOException {
        request.setAttribute("error", errorMessage);
        if (username != null) request.setAttribute("username", username);
        if (email != null) request.setAttribute("email", email);
        if (role != null) request.setAttribute("role", role);
        request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
    }
}
