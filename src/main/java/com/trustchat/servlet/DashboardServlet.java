package com.trustchat.servlet;

import com.trustchat.dao.MessageDAO;
import com.trustchat.dao.UserDAO;
import com.trustchat.model.Message;
import com.trustchat.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private UserDAO    userDAO;
    private MessageDAO messageDAO;

    @Override
    public void init() throws ServletException {
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

        List<User> users = userDAO.getAllUsersExcept(currentUser.getUserId());
        int unreadCount = messageDAO.getUnreadCount(currentUser.getUserId());

        Map<Integer, Integer> unreadPerSender =
                messageDAO.getUnreadCountPerSender(currentUser.getUserId());

        Map<Integer, Message> lastMessages = new HashMap<>();
        for (User user : users) {
            Message last = messageDAO.getLastMessage(currentUser.getUserId(), user.getUserId());
            if (last != null) {
                lastMessages.put(user.getUserId(), last);
            }
        }

        request.setAttribute("users", users);
        request.setAttribute("unreadCount", unreadCount);
        request.setAttribute("unreadPerSender", unreadPerSender);
        request.setAttribute("lastMessages", lastMessages);

        request.getRequestDispatcher("/WEB-INF/jsp/dashboard.jsp").forward(request, response);
    }
}
