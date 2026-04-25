package com.institute.dao;

import com.institute.config.DBConnectionUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompanyDAO {

    public List<Map<String, Object>> getAllCompanies() {
        List<Map<String, Object>> companies = new ArrayList<>();
        String sql = "SELECT company_id, company_name, industry, website FROM Companies";
        try (Connection conn = DBConnectionUtil.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("company_id", rs.getInt("company_id"));
                row.put("company_name", rs.getString("company_name"));
                row.put("industry", rs.getString("industry"));
                row.put("website", rs.getString("website"));
                companies.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return companies;
    }

    public boolean addCompany(String username, String passwordHash, String email, String companyName, String industry, String website) {
        String insertUserSql = "INSERT INTO Users (username, password_hash, role, email) VALUES (?, ?, 'COMPANY', ?)";
        String insertCompanySql = "INSERT INTO Companies (user_id, company_name, industry, website) VALUES (?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = DBConnectionUtil.getInstance().getConnection();
            conn.setAutoCommit(false);
            
            // Insert User
            try (PreparedStatement stmt1 = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt1.setString(1, username);
                stmt1.setString(2, passwordHash);
                stmt1.setString(3, email);
                stmt1.executeUpdate();
                
                try (ResultSet rs = stmt1.getGeneratedKeys()) {
                    if (rs.next()) {
                        int userId = rs.getInt(1);
                        // Insert Company
                        try (PreparedStatement stmt2 = conn.prepareStatement(insertCompanySql)) {
                            stmt2.setInt(1, userId);
                            stmt2.setString(2, companyName);
                            stmt2.setString(3, industry);
                            stmt2.setString(4, website);
                            stmt2.executeUpdate();
                        }
                    }
                }
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public boolean deleteCompany(int companyId) {
        // Because of ON DELETE CASCADE on Users, we should ideally delete the User.
        // But for simplicity, we delete the company and its user.
        String findUserSql = "SELECT user_id FROM Companies WHERE company_id = ?";
        String deleteUserSql = "DELETE FROM Users WHERE user_id = ?";
        
        try (Connection conn = DBConnectionUtil.getInstance().getConnection()) {
            int userId = -1;
            try (PreparedStatement stmt1 = conn.prepareStatement(findUserSql)) {
                stmt1.setInt(1, companyId);
                try (ResultSet rs = stmt1.executeQuery()) {
                    if (rs.next()) userId = rs.getInt("user_id");
                }
            }
            if (userId != -1) {
                try (PreparedStatement stmt2 = conn.prepareStatement(deleteUserSql)) {
                    stmt2.setInt(1, userId);
                    stmt2.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
