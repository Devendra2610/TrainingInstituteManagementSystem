package com.institute.controller;

import com.institute.config.DBConnectionUtil;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet("/exam/security")
public class SecurityServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int studentId = (Integer) session.getAttribute("userId");
        String eventType = request.getParameter("event");
        int examId = Integer.parseInt(request.getParameter("examId"));
        
        String actionDescription = "Suspicious Activity: " + eventType;

        String sql = "INSERT INTO audit_logs (user_id, action, action_time, details) VALUES (?, ?, CURRENT_TIMESTAMP, ?)";

        try (Connection conn = DBConnectionUtil.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setInt(1, studentId);
            stmt.setString(2, "EXAM_WARNING");
            stmt.setString(3, actionDescription + " during Exam ID " + examId);
            
            stmt.executeUpdate();
            
            response.setStatus(HttpServletResponse.SC_OK);
            
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
