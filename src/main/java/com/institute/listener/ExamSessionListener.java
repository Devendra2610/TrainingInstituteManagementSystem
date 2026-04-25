package com.institute.listener;

import com.institute.dao.ExamDAO;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Map;

@WebListener
public class ExamSessionListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) { 
        // Session created
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        
        Integer userId = (Integer) session.getAttribute("userId");
        Integer examId = (Integer) session.getAttribute("examId");
        
        // If an exam was active when the session died (e.g. timeout or forced invalidation due to multiple logins)
        if (userId != null) {
            // Unset logged in status in database
            String sql = "UPDATE users SET is_logged_in = FALSE WHERE user_id = ?";
            try (java.sql.Connection conn = com.institute.config.DBConnectionUtil.getInstance().getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.executeUpdate();
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }

            if (examId != null) {
                @SuppressWarnings("unchecked")
                Map<Integer, String> answers = (Map<Integer, String>) session.getAttribute("savedAnswers");
                if (answers != null && !answers.isEmpty()) {
                    System.out.println("Auto-submitting exam for user " + userId + " due to session invalidation.");
                    ExamDAO examDAO = new ExamDAO();
                    examDAO.submitExam(userId, examId, answers);
                }
            }
        }
    }
}
