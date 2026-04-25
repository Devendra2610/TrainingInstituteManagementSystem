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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    // Thread-safe map to maintain Single-Active-Session policy
    // Maps User ID -> HttpSession
    private static final ConcurrentHashMap<Integer, HttpSession> activeSessions = new ConcurrentHashMap<>();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            
        String username = request.getParameter("username");
        String passwordHash = request.getParameter("password"); // In real app, hash and compare

        try (Connection conn = DBConnectionUtil.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT user_id, role FROM users WHERE username = ? AND password_hash = ?")) {
             
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String role = rs.getString("role");

                    // Implement Single-Active-Session Policy
                    HttpSession existingSession = activeSessions.get(userId);
                    if (existingSession != null) {
                        try {
                            existingSession.invalidate(); // Invalidate the previous session from another location
                        } catch (IllegalStateException e) {
                            // Session was already invalidated
                        }
                    }

                    // Create new session
                    HttpSession newSession = request.getSession(true);
                    newSession.setAttribute("userId", userId);
                    newSession.setAttribute("role", role);
                    newSession.setAttribute("ipAddress", request.getRemoteAddr()); // Add IP Binding
                    activeSessions.put(userId, newSession);

                    // Track session in DB
                    logSession(userId, request.getRemoteAddr());
                    updateUserLoginState(userId, true);

                    // Redirect based on role
                    if ("ADMIN".equals(role)) {
                        response.sendRedirect("admin_reports.jsp");
                    } else {
                        response.sendRedirect("student_dashboard.jsp");
                    }

                } else {
                    request.setAttribute("errorMessage", "Invalid credentials");
                    request.getRequestDispatcher("login.jsp").forward(request, response);
                }
            }

        } catch (SQLException e) {
            throw new ServletException("Database access error", e);
        }
    }

    private void logSession(int userId, String ipAddress) {
        String sql = "INSERT INTO AuditLogs (user_id, action, ip_address) VALUES (?, 'LOGIN', ?)";
        try (Connection conn = DBConnectionUtil.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, ipAddress);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateUserLoginState(int userId, boolean isLoggedIn) {
        String sql = "UPDATE users SET is_logged_in = ?, last_login = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (Connection conn = DBConnectionUtil.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, isLoggedIn);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
