package com.institute.dao;

import com.institute.config.DBConnectionUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationDAO {

    public List<Map<String, Object>> getAllApplications() {
        List<Map<String, Object>> applications = new ArrayList<>();
        String sql = "SELECT a.application_id, s.first_name, s.last_name, s.cgpa, i.role_title, c.company_name, a.status " +
                     "FROM Applications a " +
                     "JOIN Students s ON a.student_id = s.student_id " +
                     "JOIN Internships i ON a.internship_id = i.internship_id " +
                     "JOIN Companies c ON i.company_id = c.company_id " +
                     "ORDER BY a.application_date DESC";
        
        try (Connection conn = DBConnectionUtil.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("application_id", rs.getInt("application_id"));
                row.put("student_name", rs.getString("first_name") + " " + rs.getString("last_name"));
                row.put("cgpa", rs.getDouble("cgpa"));
                row.put("role_title", rs.getString("role_title"));
                row.put("company_name", rs.getString("company_name"));
                row.put("status", rs.getString("status"));
                applications.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return applications;
    }

    public List<Map<String, Object>> getStudentApplications(int studentId) {
        List<Map<String, Object>> apps = new ArrayList<>();
        String sql = "SELECT a.application_id, i.role_title, c.company_name, a.status " +
                     "FROM Applications a " +
                     "JOIN Internships i ON a.internship_id = i.internship_id " +
                     "JOIN Companies c ON i.company_id = c.company_id " +
                     "WHERE a.student_id = ? ORDER BY a.application_date DESC";
        try (Connection conn = DBConnectionUtil.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("application_id", rs.getInt("application_id"));
                    row.put("role_title", rs.getString("role_title"));
                    row.put("company_name", rs.getString("company_name"));
                    row.put("status", rs.getString("status"));
                    apps.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return apps;
    }

    public boolean updateApplicationStatus(int applicationId, String status) {
        String sql = "UPDATE Applications SET status = ? WHERE application_id = ?";
        try (Connection conn = DBConnectionUtil.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, applicationId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
