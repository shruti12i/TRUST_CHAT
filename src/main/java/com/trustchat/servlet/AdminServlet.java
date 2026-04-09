package com.trustchat.servlet;

import com.trustchat.dao.MessageDAO;
import com.trustchat.dao.PolicyDAO;
import com.trustchat.dao.UserDAO;
import com.trustchat.model.Message;
import com.trustchat.model.PolicyRule;
import com.trustchat.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {

    private PolicyDAO  policyDAO;
    private UserDAO    userDAO;
    private MessageDAO messageDAO;

    @Override
    public void init() throws ServletException {
        policyDAO  = new PolicyDAO();
        userDAO    = new UserDAO();
        messageDAO = new MessageDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User currentUser = (User) session.getAttribute("user");

        if (!currentUser.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        List<PolicyRule> rules = policyDAO.getAllRules();
        List<User> users = userDAO.getAllUsers();
        List<Message> blockedMessages = messageDAO.getAllBlockedMessages();

        String adminSuccess = (String) request.getSession().getAttribute("adminSuccess");
        String adminError   = (String) request.getSession().getAttribute("adminError");
        if (adminSuccess != null) {
            request.setAttribute("success", adminSuccess);
            request.getSession().removeAttribute("adminSuccess");
        }
        if (adminError != null) {
            request.setAttribute("error", adminError);
            request.getSession().removeAttribute("adminError");
        }

        request.setAttribute("rules", rules);
        request.setAttribute("users", users);
        request.setAttribute("blockedMessages", blockedMessages);

        request.getRequestDispatcher("/WEB-INF/jsp/admin.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User currentUser = (User) session.getAttribute("user");

        if (!currentUser.isAdmin()) {
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
