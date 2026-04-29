package com.trustchat.servlet;

import com.trustchat.dao.MessageDAO;
import com.trustchat.dao.PolicyDAO;
import com.trustchat.dao.UserDAO;
import com.trustchat.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {

    private PolicyDAO policyDAO;
    private UserDAO userDAO;
    private MessageDAO messageDAO;

    @Override
    public void init() throws ServletException {
        policyDAO = new PolicyDAO();
        userDAO = new UserDAO();
        messageDAO = new MessageDAO();
    }

    private boolean isUserAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) return false;
        User user = (User) session.getAttribute("user");
        return user.isAdmin();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isUserAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        HttpSession session = request.getSession(false);
        String adminSuccess = (String) session.getAttribute("adminSuccess");
        String adminError = (String) session.getAttribute("adminError");

        if (adminSuccess != null) {
            request.setAttribute("success", adminSuccess);
            session.removeAttribute("adminSuccess");
        }
        if (adminError != null) {
            request.setAttribute("error", adminError);
            session.removeAttribute("adminError");
        }

        request.setAttribute("rules", policyDAO.getAllRules());
        request.setAttribute("users", userDAO.getAllUsers());
        request.setAttribute("blockedMessages", messageDAO.getAllBlockedMessages());

        request.getRequestDispatcher("/WEB-INF/jsp/admin.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isUserAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        String action = request.getParameter("action");

        if ("toggleRule".equals(action)) {
            String ruleIdStr = request.getParameter("ruleId");
            if (ruleIdStr != null) {
                try {
                    int ruleId = Integer.parseInt(ruleIdStr);
                    policyDAO.toggleRuleStatus(ruleId);
                } catch (NumberFormatException e) {
                    request.setAttribute("error", "Invalid rule ID");
                }
            }
        }

        response.sendRedirect(request.getContextPath() + "/admin");
    }
}
