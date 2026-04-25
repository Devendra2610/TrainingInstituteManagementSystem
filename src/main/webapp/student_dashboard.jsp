<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.institute.dao.StudentDAO" %>
<%@ page import="com.institute.dao.InternshipDAO" %>
<%@ page import="com.institute.dao.ApplicationDAO" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    Integer userId = (Integer) session.getAttribute("userId");
    if (userId == null || !"STUDENT".equals(session.getAttribute("role"))) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }

    StudentDAO studentDAO = new StudentDAO();
    InternshipDAO internshipDAO = new InternshipDAO();
    ApplicationDAO applicationDAO = new ApplicationDAO();
    
    double cgpa = studentDAO.getStudentCgpa(userId);
    List<Map<String, Object>> internships = internshipDAO.getEligibleInternships(cgpa);
    List<Map<String, Object>> myApplications = applicationDAO.getStudentApplications(userId);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Student Dashboard</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f9; color: #333; margin: 0; padding: 20px; }
        .container { max-width: 1000px; margin: auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
        h1 { color: #2c3e50; }
        .cgpa-banner { background-color: #3498db; color: white; padding: 10px 15px; border-radius: 5px; margin-bottom: 20px; font-weight: bold; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
        th { background-color: #f2f2f2; color: #2c3e50; }
        .btn-apply { background-color: #2ecc71; color: white; border: none; padding: 8px 12px; border-radius: 4px; cursor: pointer; }
        .btn-apply:hover { background-color: #27ae60; }
    </style>
</head>
<body>
    <div class="container">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <h1>Student Dashboard</h1>
            <div>
                <a href="profile.jsp" style="text-decoration: none; padding: 10px 15px; background: #9b59b6; color: white; border-radius: 5px; font-weight: bold; margin-right: 10px;">My Profile</a>
                <a href="${pageContext.request.contextPath}/logout" style="text-decoration: none; padding: 10px 15px; background: #e74c3c; color: white; border-radius: 5px; font-weight: bold;">Logout</a>
            </div>
        </div>
        <div class="cgpa-banner">Your Current CGPA: <%= String.format("%.2f", cgpa) %></div>
        
        <h2>Eligible Internships</h2>
        <% if (internships.isEmpty()) { %>
            <p>No internships currently available for your CGPA level.</p>
        <% } else { %>
            <table>
                <thead>
                    <tr>
                        <th>Title</th>
                        <th>Description</th>
                        <th>Required CGPA</th>
                        <th>Deadline</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Map<String, Object> internship : internships) { %>
                        <tr>
                            <td><%= internship.get("title") %></td>
                            <td><%= internship.get("description") %></td>
                            <td><%= internship.get("eligibility_cgpa") %></td>
                            <td><%= internship.get("deadline") %></td>
                            <td>
                                <form action="<%= request.getContextPath() %>/student/apply" method="post" onsubmit="return confirm('Confirm application?');">
                                    <input type="hidden" name="internshipId" value="<%= internship.get("id") %>">
                                    <button type="submit" class="btn-apply">Apply Now</button>
                                </form>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        <% } %>

        <h2 style="margin-top: 40px;">My Applications</h2>
        <% if (myApplications.isEmpty()) { %>
            <p>You haven't applied to any internships yet.</p>
        <% } else { %>
            <table>
                <thead>
                    <tr>
                        <th>Role Title</th>
                        <th>Company</th>
                        <th>Status</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Map<String, Object> app : myApplications) { %>
                        <tr>
                            <td><%= app.get("role_title") %></td>
                            <td><%= app.get("company_name") %></td>
                            <td>
                                <span style="font-weight: bold; color: <%= "SELECTED".equals(app.get("status")) ? "green" : "REJECTED".equals(app.get("status")) ? "red" : "blue" %>;">
                                    <%= app.get("status") %>
                                </span>
                            </td>
                            <td>
                                <% if ("SELECTED".equals(app.get("status"))) { %>
                                    <form action="<%= request.getContextPath() %>/student/startExam" method="post">
                                        <button type="submit" class="btn-apply" style="background-color: #8e44ad;">Take Exam</button>
                                    </form>
                                <% } else { %>
                                    <span>N/A</span>
                                <% } %>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        <% } %>
    </div>
</body>
</html>
