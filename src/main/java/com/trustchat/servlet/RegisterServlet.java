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

        String username        = request.getParameter("username");
        String password        = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String email           = request.getParameter("email");
        String role            = request.getParameter("role");

        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            email    == null || email.trim().isEmpty()    ||
            role     == null || role.trim().isEmpty()) {

            request.setAttribute("error", "All fields are required");
            preserveFormValues(request, username, email, role);
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
            return;
        }

        String roleLower = role.trim().toLowerCase();
        if (!roleLower.equals("student") && !roleLower.equals("teacher")) {
            request.setAttribute("error", "Invalid role. Only Student or Teacher can register.");
            preserveFormValues(request, username, email, "");
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match");
            preserveFormValues(request, username, email, role);
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
            return;
        }

        if (password.length() < 6) {
            request.setAttribute("error", "Password must be at least 6 characters");
            preserveFormValues(request, username, email, role);
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
            return;
        }

        if (userDAO.usernameExists(username.trim())) {
            request.setAttribute("error", "Username already exists");
            preserveFormValues(request, null, email, role);
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
            return;
        }

        if (userDAO.emailExists(email.trim())) {
            request.setAttribute("error", "Email already registered");
            preserveFormValues(request, username, null, role);
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
            return;
        }

        User newUser = new User(username.trim(), password, email.trim(), roleLower);

        if (userDAO.register(newUser)) {
            request.setAttribute("success", "Registration successful! Please login.");
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
        } else {
            request.setAttribute("error", "Registration failed. Please try again.");
            preserveFormValues(request, username, email, role);
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
        }
    }

    private void preserveFormValues(HttpServletRequest request,
                                    String username, String email, String role) {
        if (username != null) request.setAttribute("username", username);
        if (email    != null) request.setAttribute("email", email);
        if (role     != null) request.setAttribute("role", role);
    }
}
