package com.institute.dao;

import com.institute.config.DBConnectionUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class ExamDAO {

    /**
     * Submits an exam using Transaction Management (manual commit/rollback).
     * Ensures both the attempt record and all answers are saved atomically.
     * 
     * @param studentId The ID of the student submitting the exam
     * @param examId The ID of the exam
     * @param answers A Map where Key = question_id and Value = submitted_answer
     * @return true if successful, false otherwise
     */
    public boolean submitExam(int studentId, int examId, Map<Integer, String> answers) {
        Connection conn = null;
        PreparedStatement attemptStmt = null;
        PreparedStatement answerStmt = null;
        ResultSet generatedKeys = null;
        boolean success = false;

        try {
            conn = DBConnectionUtil.getInstance().getConnection();
            
            // 1. Disable Auto-Commit for Transaction Management
            conn.setAutoCommit(false);

            // 2. Insert into exam_attempts
            String insertAttemptSQL = "INSERT INTO exam_attempts (student_id, exam_id, start_time, end_time, status) VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'COMPLETED')";
            attemptStmt = conn.prepareStatement(insertAttemptSQL, Statement.RETURN_GENERATED_KEYS);
            attemptStmt.setInt(1, studentId);
            attemptStmt.setInt(2, examId);
            
            int affectedRows = attemptStmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating exam attempt failed, no rows affected.");
            }

            int attemptId = -1;
            generatedKeys = attemptStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                attemptId = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Creating exam attempt failed, no ID obtained.");
            }

            // 3. Insert into answers (Batch Processing)
            String insertAnswerSQL = "INSERT INTO answers (attempt_id, question_id, submitted_answer) VALUES (?, ?, ?)";
            answerStmt = conn.prepareStatement(insertAnswerSQL);

            for (Map.Entry<Integer, String> entry : answers.entrySet()) {
                answerStmt.setInt(1, attemptId);
                answerStmt.setInt(2, entry.getKey()); // Question ID
                answerStmt.setString(3, entry.getValue()); // Student's Answer
                answerStmt.addBatch();
            }

            // Execute the batch of insert statements
            answerStmt.executeBatch();

            // 4. Commit the Transaction
            conn.commit();
            success = true;

        } catch (SQLException e) {
            // 5. Rollback on Exception
            if (conn != null) {
                try {
                    System.err.println("Transaction is being rolled back due to error: " + e.getMessage());
                    conn.rollback();
                } catch (SQLException excep) {
                    System.err.println("Error rolling back transaction: " + excep.getMessage());
                }
            }
            e.printStackTrace();
        } finally {
            // 6. Clean up resources and restore Auto-Commit
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (answerStmt != null) answerStmt.close();
                if (attemptStmt != null) attemptStmt.close();
                if (conn != null) {
                    // Restore default auto-commit behavior
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        
        return success;
    }

    public java.util.List<Map<String, Object>> getQuestions(int examId) {
        java.util.List<Map<String, Object>> questions = new java.util.ArrayList<>();
        String sql = "SELECT * FROM Questions WHERE exam_id = ?";
        try (Connection conn = DBConnectionUtil.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> q = new java.util.HashMap<>();
                    q.put("question_id", rs.getInt("question_id"));
                    q.put("question_text", rs.getString("question_text"));
                    q.put("question_type", rs.getString("question_type"));
                    q.put("option_a", rs.getString("option_a"));
                    q.put("option_b", rs.getString("option_b"));
                    q.put("option_c", rs.getString("option_c"));
                    q.put("option_d", rs.getString("option_d"));
                    questions.add(q);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }

    public java.util.List<Map<String, Object>> getCompletedExams() {
        java.util.List<Map<String, Object>> exams = new java.util.ArrayList<>();
        String sql = "SELECT ea.attempt_id, s.first_name, s.last_name, ex.title, a.application_id, ea.marks_obtained " +
                     "FROM exam_attempts ea " +
                     "JOIN Students s ON ea.student_id = s.student_id " +
                     "JOIN Exams ex ON ea.exam_id = ex.exam_id " +
                     "JOIN Internships i ON ex.internship_id = i.internship_id " +
                     "JOIN Applications a ON a.student_id = ea.student_id AND a.internship_id = i.internship_id " +
                     "WHERE ea.status = 'COMPLETED'";
        try (Connection conn = DBConnectionUtil.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new java.util.HashMap<>();
                row.put("attempt_id", rs.getInt("attempt_id"));
                row.put("student_name", rs.getString("first_name") + " " + rs.getString("last_name"));
                row.put("exam_title", rs.getString("title"));
                row.put("application_id", rs.getInt("application_id"));
                row.put("mcq_marks", rs.getInt("marks_obtained"));
                exams.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exams;
    }

    public java.util.List<Map<String, Object>> getSubjectiveAnswers(int attemptId) {
        java.util.List<Map<String, Object>> answers = new java.util.ArrayList<>();
        String sql = "SELECT a.answer_id, q.question_text, a.submitted_answer, q.marks as max_marks " +
                     "FROM answers a " +
                     "JOIN Questions q ON a.question_id = q.question_id " +
                     "WHERE a.attempt_id = ? AND q.question_type = 'SUBJECTIVE'";
        try (Connection conn = DBConnectionUtil.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, attemptId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new java.util.HashMap<>();
                    row.put("answer_id", rs.getInt("answer_id"));
                    row.put("question_text", rs.getString("question_text"));
                    row.put("submitted_answer", rs.getString("submitted_answer"));
                    row.put("max_marks", rs.getInt("max_marks"));
                    answers.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return answers;
    }

    public boolean finalizeEvaluation(int attemptId, int applicationId, Map<Integer, Integer> subjectiveMarks, String finalStatus) {
        Connection conn = null;
        try {
            conn = DBConnectionUtil.getInstance().getConnection();
            conn.setAutoCommit(false);
            
            // 1. Update marks for subjective answers
            String updateAnswerSQL = "UPDATE answers SET marks_awarded = ? WHERE answer_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateAnswerSQL)) {
                for (Map.Entry<Integer, Integer> entry : subjectiveMarks.entrySet()) {
                    stmt.setInt(1, entry.getValue());
                    stmt.setInt(2, entry.getKey());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            // 2. Recalculate total marks
            String updateTotalSQL = "UPDATE exam_attempts ea SET marks_obtained = (SELECT COALESCE(SUM(marks_awarded),0) FROM answers WHERE attempt_id = ea.attempt_id), status = 'EVALUATED' WHERE attempt_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateTotalSQL)) {
                stmt.setInt(1, attemptId);
                stmt.executeUpdate();
            }

            // 3. Update application status
            String updateAppSQL = "UPDATE Applications SET status = ? WHERE application_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateAppSQL)) {
                stmt.setString(1, finalStatus);
                stmt.setInt(2, applicationId);
                stmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {}
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) {}
            }
        }
    }
}
