<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.institute.dao.ExamDAO" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    String role = (String) session.getAttribute("role");
    if (!"ADMIN".equals(role)) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Admins Only");
        return;
    }

    int attemptId = Integer.parseInt(request.getParameter("attemptId"));
    int applicationId = Integer.parseInt(request.getParameter("applicationId"));
    String studentName = request.getParameter("studentName");
    int mcqMarks = Integer.parseInt(request.getParameter("mcqMarks"));

    ExamDAO examDAO = new ExamDAO();
    List<Map<String, Object>> answers = examDAO.getSubjectiveAnswers(attemptId);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Evaluate Exam - <%= studentName %></title>
    <style>
        body { font-family: 'Segoe UI', system-ui, sans-serif; background-color: #f1f5f9; padding: 20px; }
        .container { max-width: 800px; margin: auto; background: white; border-radius: 12px; padding: 30px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); }
        .header { border-bottom: 2px solid #e2e8f0; padding-bottom: 15px; margin-bottom: 25px; }
        .answer-block { background: #f8fafc; border: 1px solid #e2e8f0; padding: 15px; border-radius: 8px; margin-bottom: 20px; }
        .q-text { font-weight: bold; color: #1e293b; margin-bottom: 10px; }
        .s-ans { background: white; padding: 10px; border: 1px dashed #cbd5e1; border-radius: 4px; margin-bottom: 15px; white-space: pre-wrap; }
        .score-input { padding: 8px; border-radius: 4px; border: 1px solid #cbd5e1; width: 80px; }
        .btn { padding: 10px 20px; border-radius: 6px; border: none; font-weight: bold; cursor: pointer; color: white; }
        .btn-success { background-color: #10b981; }
        .form-group { margin-top: 20px; padding-top: 20px; border-top: 1px solid #e2e8f0; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h2>Evaluate Subjective Answers</h2>
            <p><strong>Student:</strong> <%= studentName %></p>
            <p><strong>Auto-Graded MCQ Marks:</strong> <%= mcqMarks %></p>
        </div>

        <form action="${pageContext.request.contextPath}/admin/action" method="post">
            <input type="hidden" name="action" value="submitEvaluation">
            <input type="hidden" name="attemptId" value="<%= attemptId %>">
            <input type="hidden" name="applicationId" value="<%= applicationId %>">
            
            <% if(answers.isEmpty()) { %>
                <p>No subjective questions to evaluate for this exam.</p>
            <% } else { %>
                <% for (Map<String, Object> ans : answers) { %>
                    <div class="answer-block">
                        <div class="q-text">Q: <%= ans.get("question_text") %> (Max: <%= ans.get("max_marks") %> marks)</div>
                        <div class="s-ans"><%= ans.get("submitted_answer") != null ? ans.get("submitted_answer") : "<i>No answer provided</i>" %></div>
                        <label><strong>Award Marks: </strong></label>
                        <input type="number" name="mark_<%= ans.get("answer_id") %>" class="score-input" min="0" max="<%= ans.get("max_marks") %>" required>
                    </div>
                <% } %>
            <% } %>

            <div class="form-group">
                <label><strong>Final Decision for Internship:</strong></label><br>
                <select name="finalStatus" style="padding: 10px; border-radius: 4px; border: 1px solid #cbd5e1; width: 100%; margin-top: 10px; margin-bottom: 20px;">
                    <option value="SELECTED">SELECTED (Offer Internship)</option>
                    <option value="REJECTED">REJECTED (Did not pass exam)</option>
                </select>
                
                <button type="submit" class="btn btn-success" style="width: 100%;">Submit Final Evaluation</button>
            </div>
        </form>
    </div>
</body>
</html>
