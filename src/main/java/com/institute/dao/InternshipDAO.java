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

public class InternshipDAO {

    /**
     * Fetches internships where the required CGPA is less than or equal to the student's CGPA.
     */
    public List<Map<String, Object>> getEligibleInternships(double studentCgpa) {
        List<Map<String, Object>> internships = new ArrayList<>();
        String sql = "SELECT internship_id, role_title, company_id, description, min_cgpa, deadline FROM internships WHERE min_cgpa <= ? AND deadline >= CURRENT_DATE() AND status = 'OPEN'";

        try (Connection conn = DBConnectionUtil.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, studentCgpa);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getInt("internship_id"));
                    row.put("title", rs.getString("role_title"));
                    row.put("company_id", rs.getInt("company_id"));
                    row.put("description", rs.getString("description"));
                    row.put("eligibility_cgpa", rs.getDouble("min_cgpa"));
                    row.put("deadline", rs.getDate("deadline"));
                    internships.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return internships;
    }

    /**
     * Checks if a student has already applied for an internship.
     */
    public boolean hasApplied(int studentId, int internshipId) {
        String sql = "SELECT 1 FROM applications WHERE student_id = ? AND internship_id = ?";
        boolean applied = false;

        try (Connection conn = DBConnectionUtil.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setInt(2, internshipId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    applied = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return applied;
    }

    /**
     * Applies for an internship using a transaction to ensure an application and its audit log
     * are created together.
     */
    public boolean applyForInternship(int studentId, int internshipId) {
        Connection conn = null;
        PreparedStatement applyStmt = null;
        PreparedStatement logStmt = null;
        boolean success = false;

        try {
            conn = DBConnectionUtil.getInstance().getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Insert into applications
            String applySql = "INSERT INTO applications (student_id, internship_id, status, application_date) VALUES (?, ?, 'APPLIED', CURRENT_TIMESTAMP)";
            applyStmt = conn.prepareStatement(applySql);
            applyStmt.setInt(1, studentId);
            applyStmt.setInt(2, internshipId);
            int rows1 = applyStmt.executeUpdate();

            // 2. Insert into AuditLogs
            String logSql = "INSERT INTO AuditLogs (user_id, action) VALUES (?, ?)";
            logStmt = conn.prepareStatement(logSql);
            logStmt.setInt(1, studentId);
            logStmt.setString(2, "APPLIED_INTERNSHIP_" + internshipId);
            int rows2 = logStmt.executeUpdate();

            if (rows1 > 0 && rows2 > 0) {
                conn.commit(); // Commit transaction
                success = true;
            } else {
                conn.rollback(); // Rollback if any insert fails
            }

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            try {
                if (applyStmt != null) applyStmt.close();
                if (logStmt != null) logStmt.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return success;
    }

    public boolean addInternship(int companyId, String roleTitle, String description, double stipend, double minCgpa, String deadline) {
        String sql = "INSERT INTO Internships (company_id, role_title, description, stipend, min_cgpa, deadline) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, companyId);
            stmt.setString(2, roleTitle);
            stmt.setString(3, description);
            stmt.setDouble(4, stipend);
            stmt.setDouble(5, minCgpa);
            stmt.setDate(6, java.sql.Date.valueOf(deadline));
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Map<String, Object>> getAllInternships() {
        List<Map<String, Object>> internships = new ArrayList<>();
        String sql = "SELECT i.internship_id, i.role_title, c.company_name, i.min_cgpa, i.deadline, i.status FROM Internships i JOIN Companies c ON i.company_id = c.company_id ORDER BY i.created_at DESC";
        try (Connection conn = DBConnectionUtil.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("internship_id", rs.getInt("internship_id"));
                row.put("role_title", rs.getString("role_title"));
                row.put("company_name", rs.getString("company_name"));
                row.put("min_cgpa", rs.getDouble("min_cgpa"));
                row.put("deadline", rs.getDate("deadline"));
                row.put("status", rs.getString("status"));
                internships.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return internships;
    }
}
