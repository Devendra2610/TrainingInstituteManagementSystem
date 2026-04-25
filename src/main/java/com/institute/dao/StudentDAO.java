package com.institute.dao;

import com.institute.config.DBConnectionUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentDAO {

    /**
     * Retrieves the CGPA of a student given their ID.
     */
    public double getStudentCgpa(int studentId) {
        double cgpa = 0.0;
        String sql = "SELECT cgpa FROM students WHERE student_id = ?";

        try (Connection conn = DBConnectionUtil.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    cgpa = rs.getDouble("cgpa");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cgpa;
    }
}
