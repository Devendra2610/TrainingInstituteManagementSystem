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

@WebServlet("/student/updateProfile")
public class UpdateProfileServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        int studentId = (Integer) session.getAttribute("userId");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String department = request.getParameter("department");
        String cgpaStr = request.getParameter("cgpa");
        String resumeUrl = request.getParameter("resumeUrl");

        double cgpa = 0.0;
        try {
            cgpa = Double.parseDouble(cgpaStr);
        } catch (NumberFormatException e) {
            // handle error
        }

        String sql = "INSERT INTO Students (student_id, first_name, last_name, department, cgpa, resume_url) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), " +
                     "department = VALUES(department), cgpa = VALUES(cgpa), resume_url = VALUES(resume_url)";

        try (Connection conn = DBConnectionUtil.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setInt(1, studentId);
            stmt.setString(2, firstName);
            stmt.setString(3, lastName);
            stmt.setString(4, department);
            stmt.setDouble(5, cgpa);
            stmt.setString(6, resumeUrl);
            
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new ServletException("Database access error", e);
        }

        response.sendRedirect(request.getContextPath() + "/profile.jsp?success=true");
    }
}
