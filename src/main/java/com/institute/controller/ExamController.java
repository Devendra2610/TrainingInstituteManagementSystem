package com.institute.controller;

import com.institute.config.DBConnectionUtil;
import com.institute.dao.ExamDAO;
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
import java.util.HashMap;
import java.util.Map;

@WebServlet("/exam/controller")
public class ExamController extends HttpServlet {

    private ExamDAO examDAO;

    @Override
    public void init() throws ServletException {
        examDAO = new ExamDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            
        String action = request.getParameter("action");

        if ("autosave".equals(action)) {
            handleAutoSave(request, response);
        } else if ("submit".equals(action)) {
            handleSubmit(request, response);
        } else if ("logCheat".equals(action)) {
            handleLogCheat(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Action");
        }
    }

    private void handleLogCheat(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            int studentId = (Integer) session.getAttribute("userId");
            String ip = request.getRemoteAddr();
            String sql = "INSERT INTO AuditLogs (user_id, action, ip_address) VALUES (?, 'TAB_SWITCH_DETECTED', ?)";
            try (Connection conn = DBConnectionUtil.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, studentId);
                stmt.setString(2, ip);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleAutoSave(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Just store the answer in the session for now (or a temp table)
        // AJAX-enabled Servlet for auto-save functionality as students navigate
        HttpSession session = request.getSession(false);
        int questionId = Integer.parseInt(request.getParameter("questionId"));
        String answer = request.getParameter("answer");

        @SuppressWarnings("unchecked")
        Map<Integer, String> savedAnswers = (Map<Integer, String>) session.getAttribute("savedAnswers");
        if (savedAnswers == null) {
            savedAnswers = new HashMap<>();
            session.setAttribute("savedAnswers", savedAnswers);
        }
        
        savedAnswers.put(questionId, answer);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("Saved");
    }

    private void handleSubmit(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        int studentId = (Integer) session.getAttribute("userId");
        int examId = Integer.parseInt(request.getParameter("examId"));
        long startTime = (Long) session.getAttribute("examStartTime");
        int durationMinutes = (Integer) session.getAttribute("examDuration");

        // 1. Server-side timer validation
        long currentTime = System.currentTimeMillis();
        long allowedTimeInMillis = durationMinutes * 60 * 1000L;
        // Adding 2 minutes buffer for network latency
        if ((currentTime - startTime) > (allowedTimeInMillis + 120000)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Exam time limit exceeded.");
            return;
        }

        @SuppressWarnings("unchecked")
        Map<Integer, String> answers = (Map<Integer, String>) session.getAttribute("savedAnswers");
        if (answers == null) {
            answers = new HashMap<>();
        }

        // 2. Submit via DAO (Transaction Management)
        boolean success = examDAO.submitExam(studentId, examId, answers);

        if (success) {
            // 3. Evaluation Engine
            evaluateExam(studentId, examId);
            session.removeAttribute("savedAnswers");
            response.sendRedirect(request.getContextPath() + "/student/dashboard.jsp");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Submission failed.");
        }
    }

    private void evaluateExam(int studentId, int examId) {
        // Evaluation Engine: MCQs auto-graded via SQL joins
        String sql = "UPDATE answers a " +
                     "JOIN questions q ON a.question_id = q.id " +
                     "JOIN exam_attempts ea ON a.attempt_id = ea.id " +
                     "SET a.is_correct = (a.submitted_answer = q.correct_option), " +
                     "    a.score = CASE WHEN a.submitted_answer = q.correct_option THEN q.marks ELSE 0 END " +
                     "WHERE ea.student_id = ? AND ea.exam_id = ? AND q.question_type = 'MCQ'";

        try (Connection conn = DBConnectionUtil.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, examId);
            stmt.executeUpdate();
            
            // Note: Subjective questions remain untouched (e.g., is_correct = NULL) for Admin review.
        } catch (SQLException e) {
            e.printStackTrace(); // Log error, but submission was already successful
        }
    }
}
