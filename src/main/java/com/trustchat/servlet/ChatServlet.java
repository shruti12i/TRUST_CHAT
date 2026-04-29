package com.trustchat.servlet;

import com.trustchat.dao.MessageDAO;
import com.trustchat.dao.MessageLogDAO;
import com.trustchat.dao.OfflineMessageDAO;
import com.trustchat.dao.UserDAO;
import com.trustchat.engine.PolicyEngine;
import com.trustchat.model.Message;
import com.trustchat.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/chat")
public class ChatServlet extends HttpServlet {

    private UserDAO userDAO;
    private MessageDAO messageDAO;
    private MessageLogDAO logDAO;
    private OfflineMessageDAO offlineDAO;
    private PolicyEngine policyEngine;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
        messageDAO = new MessageDAO();
        logDAO = new MessageLogDAO();
        offlineDAO = new OfflineMessageDAO();
        policyEngine = new PolicyEngine();
    }

    private boolean isUserLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("user") != null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isUserLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User currentUser = (User) request.getSession().getAttribute("user");
        String receiverIdParam = request.getParameter("receiverId");

        if (receiverIdParam == null || receiverIdParam.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        try {
            int receiverId = Integer.parseInt(receiverIdParam);
            User receiver = userDAO.getUserById(receiverId);

            if (receiver == null) {
                response.sendRedirect(request.getContextPath() + "/dashboard");
                return;
            }

            if ("student".equalsIgnoreCase(currentUser.getRole()) && "student".equalsIgnoreCase(receiver.getRole())) {
                request.getSession().setAttribute("dashboardError", "Students are not allowed to chat with other students.");
                response.sendRedirect(request.getContextPath() + "/dashboard");
                return;
            }

            messageDAO.markConversationAsRead(currentUser.getUserId(), receiverId);
            
            request.setAttribute("receiver", receiver);
            request.setAttribute("messages", messageDAO.getConversation(currentUser.getUserId(), receiverId));
            request.setAttribute("users", userDAO.getAllUsersExcept(currentUser.getUserId()));
            
            request.getRequestDispatcher("/WEB-INF/jsp/chat.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isUserLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User currentUser = (User) request.getSession().getAttribute("user");
        String receiverIdParam = request.getParameter("receiverId");
        String messageText = request.getParameter("message");

        if (receiverIdParam == null || messageText == null || messageText.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        try {
            int receiverId = Integer.parseInt(receiverIdParam);
            User receiver = userDAO.getUserById(receiverId);

            if (receiver == null) {
                response.sendRedirect(request.getContextPath() + "/dashboard");
                return;
            }

            Message message = new Message(currentUser.getUserId(), receiverId, messageText.trim());
            PolicyEngine.PolicyCheckResult result = policyEngine.checkMessage(message, currentUser, receiver);

            if (result.isBlocked()) {
                message.setBlocked(true);
                message.setBlockReason(result.getBlockReason());
                messageDAO.sendMessage(message);

                logDAO.log(currentUser.getUserId(), "MESSAGE_BLOCKED", 
                    "To: " + receiver.getUsername() + " | Rule: " + result.getTriggeredRule() + " | Reason: " + result.getBlockReason());
                
                request.setAttribute("error", "Message blocked: " + result.getBlockReason());

            } else {
                messageDAO.sendMessage(message);
                logDAO.log(currentUser.getUserId(), "MESSAGE_SENT", 
                    "To: " + receiver.getUsername() + " | MsgId: " + message.getMessageId());
                offlineDAO.queueOfflineMessage(currentUser.getUserId(), receiverId, messageText.trim());
            }

            messageDAO.markConversationAsRead(currentUser.getUserId(), receiverId);
            
            request.setAttribute("receiver", receiver);
            request.setAttribute("messages", messageDAO.getConversation(currentUser.getUserId(), receiverId));
            request.setAttribute("users", userDAO.getAllUsersExcept(currentUser.getUserId()));
            
            request.getRequestDispatcher("/WEB-INF/jsp/chat.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
        }
    }
}
