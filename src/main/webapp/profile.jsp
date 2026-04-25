<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="com.institute.config.DBConnectionUtil" %>
<%
    Integer userId = (Integer) session.getAttribute("userId");
    if (userId == null || !"STUDENT".equals(session.getAttribute("role"))) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }

    String firstName = "";
    String lastName = "";
    String department = "";
    String resumeUrl = "";
    double cgpa = 0.0;

    try (Connection conn = DBConnectionUtil.getInstance().getConnection();
         PreparedStatement stmt = conn.prepareStatement("SELECT first_name, last_name, department, cgpa, resume_url FROM Students WHERE student_id = ?")) {
        stmt.setInt(1, userId);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                firstName = rs.getString("first_name");
                lastName = rs.getString("last_name");
                department = rs.getString("department");
                cgpa = rs.getDouble("cgpa");
                resumeUrl = rs.getString("resume_url");
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>My Profile</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f9; color: #333; margin: 0; padding: 20px; }
        .container { max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
        h1 { color: #2c3e50; text-align: center; }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 5px; font-weight: bold; }
        .form-group input { width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; }
        .btn-submit { width: 100%; background-color: #3498db; color: white; border: none; padding: 12px; border-radius: 4px; cursor: pointer; font-size: 16px; margin-top: 10px; }
        .btn-submit:hover { background-color: #2980b9; }
        .back-link { display: block; text-align: center; margin-top: 20px; color: #3498db; text-decoration: none; }
        .message { padding: 10px; border-radius: 4px; margin-bottom: 15px; text-align: center; }
        .success { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
    </style>
</head>
<body>
    <div class="container">
        <h1>My Profile</h1>
        <% if (request.getParameter("success") != null) { %>
            <div class="message success">Profile updated successfully!</div>
        <% } %>
        <form action="<%= request.getContextPath() %>/student/updateProfile" method="post">
            <div class="form-group">
                <label>First Name</label>
                <input type="text" name="firstName" value="<%= firstName != null ? firstName : "" %>" required>
            </div>
            <div class="form-group">
                <label>Last Name</label>
                <input type="text" name="lastName" value="<%= lastName != null ? lastName : "" %>" required>
            </div>
            <div class="form-group">
                <label>Department</label>
                <input type="text" name="department" value="<%= department != null ? department : "" %>" required>
            </div>
            <div class="form-group">
                <label>CGPA</label>
                <input type="number" step="0.01" min="0" max="10" name="cgpa" value="<%= cgpa %>" required>
            </div>
            <div class="form-group">
                <label>Resume URL</label>
                <input type="url" name="resumeUrl" value="<%= resumeUrl != null ? resumeUrl : "" %>" placeholder="https://linkedin.com/in/yourprofile">
            </div>
            <button type="submit" class="btn-submit">Save Profile</button>
        </form>
        <a href="student_dashboard.jsp" class="back-link">Back to Dashboard</a>
    </div>
</body>
</html>
