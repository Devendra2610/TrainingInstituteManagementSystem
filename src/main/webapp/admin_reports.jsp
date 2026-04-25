<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.institute.dao.CompanyDAO" %>
<%@ page import="com.institute.dao.InternshipDAO" %>
<%@ page import="com.institute.dao.ApplicationDAO" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    String role = (String) session.getAttribute("role");
    if (!"ADMIN".equals(role)) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Admins Only");
        return;
    }

    CompanyDAO companyDAO = new CompanyDAO();
    InternshipDAO internshipDAO = new InternshipDAO();
    ApplicationDAO applicationDAO = new ApplicationDAO();
    com.institute.dao.ExamDAO examDAO = new com.institute.dao.ExamDAO();

    List<Map<String, Object>> companies = companyDAO.getAllCompanies();
    List<Map<String, Object>> internships = internshipDAO.getAllInternships();
    List<Map<String, Object>> applications = applicationDAO.getAllApplications();
    List<Map<String, Object>> completedExams = examDAO.getCompletedExams();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Admin Dashboard - Company & Internship Management</title>
    <style>
        :root {
            --primary: #2563eb;
            --surface: #ffffff;
            --background: #f1f5f9;
            --text: #1e293b;
            --border: #e2e8f0;
        }
        body { font-family: 'Segoe UI', system-ui, sans-serif; background-color: var(--background); color: var(--text); margin: 0; padding: 20px; }
        .container { max-width: 1200px; margin: auto; }
        .header-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
        .card { background: var(--surface); border-radius: 12px; padding: 24px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); margin-bottom: 24px; }
        h1, h2 { color: #0f172a; margin-top: 0; }
        table { width: 100%; border-collapse: collapse; margin-top: 15px; }
        th, td { border-bottom: 1px solid var(--border); padding: 12px; text-align: left; }
        th { background-color: #f8fafc; font-weight: 600; color: #475569; }
        .btn { padding: 8px 16px; border-radius: 6px; border: none; font-weight: 600; cursor: pointer; text-decoration: none; display: inline-block; }
        .btn-primary { background-color: var(--primary); color: white; }
        .btn-danger { background-color: #ef4444; color: white; }
        .btn-logout { background-color: #334155; color: white; }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 6px; font-weight: 500; color: #475569; }
        .form-group input, .form-group select, .form-group textarea { width: 100%; padding: 10px; border: 1px solid var(--border); border-radius: 6px; box-sizing: border-box; }
        .grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
        .message { padding: 10px; border-radius: 6px; margin-bottom: 20px; }
        .success { background-color: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
        .error { background-color: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header-bar">
            <h1>Admin Dashboard</h1>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-logout">Logout</a>
        </div>

        <% if (request.getParameter("msg") != null) { %>
            <div class="message success"><%= request.getParameter("msg") %></div>
        <% } %>
        <% if (request.getParameter("err") != null) { %>
            <div class="message error"><%= request.getParameter("err") %></div>
        <% } %>

        <div class="grid-2">
            <!-- Add Company Form -->
            <div class="card">
                <h2>Add New Company</h2>
                <form action="${pageContext.request.contextPath}/admin/action" method="post">
                    <input type="hidden" name="action" value="addCompany">
                    <div class="form-group">
                        <label>Company Name</label>
                        <input type="text" name="companyName" required>
                    </div>
                    <div class="form-group">
                        <label>Industry</label>
                        <input type="text" name="industry" required>
                    </div>
                    <div class="form-group">
                        <label>Website</label>
                        <input type="url" name="website">
                    </div>
                    <h4>Company Login Credentials</h4>
                    <div class="form-group">
                        <label>Username</label>
                        <input type="text" name="username" required>
                    </div>
                    <div class="form-group">
                        <label>Password</label>
                        <input type="password" name="password" required>
                    </div>
                    <div class="form-group">
                        <label>Email</label>
                        <input type="email" name="email" required>
                    </div>
                    <button type="submit" class="btn btn-primary">Add Company</button>
                </form>
            </div>

            <!-- Post Internship Form -->
            <div class="card">
                <h2>Post Internship</h2>
                <form action="${pageContext.request.contextPath}/admin/action" method="post">
                    <input type="hidden" name="action" value="addInternship">
                    <div class="form-group">
                        <label>Select Company</label>
                        <select name="companyId" required>
                            <option value="">-- Select a Company --</option>
                            <% for (Map<String, Object> comp : companies) { %>
                                <option value="<%= comp.get("company_id") %>"><%= comp.get("company_name") %></option>
                            <% } %>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Role Title</label>
                        <input type="text" name="roleTitle" required>
                    </div>
                    <div class="form-group">
                        <label>Description</label>
                        <textarea name="description" rows="3" required></textarea>
                    </div>
                    <div class="grid-2">
                        <div class="form-group">
                            <label>Stipend (Optional)</label>
                            <input type="number" step="0.01" name="stipend">
                        </div>
                        <div class="form-group">
                            <label>Eligibility CGPA (0-10)</label>
                            <input type="number" step="0.01" min="0" max="10" name="minCgpa" required>
                        </div>
                    </div>
                    <div class="form-group">
                        <label>Application Deadline</label>
                        <input type="date" name="deadline" required>
                    </div>
                    <button type="submit" class="btn btn-primary">Post Internship</button>
                </form>
            </div>
        </div>

        <div class="card">
            <h2>Registered Companies</h2>
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Industry</th>
                        <th>Website</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Map<String, Object> comp : companies) { %>
                        <tr>
                            <td><%= comp.get("company_id") %></td>
                            <td><%= comp.get("company_name") %></td>
                            <td><%= comp.get("industry") %></td>
                            <td><%= comp.get("website") %></td>
                            <td>
                                <form action="${pageContext.request.contextPath}/admin/action" method="post" style="display:inline;" onsubmit="return confirm('Are you sure you want to delete this company?');">
                                    <input type="hidden" name="action" value="deleteCompany">
                                    <input type="hidden" name="companyId" value="<%= comp.get("company_id") %>">
                                    <button type="submit" class="btn btn-danger" style="padding:4px 8px; font-size:0.875rem;">Delete</button>
                                </form>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>

        <div class="card">
            <h2>All Posted Internships</h2>
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Role</th>
                        <th>Company</th>
                        <th>Min CGPA</th>
                        <th>Deadline</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Map<String, Object> intern : internships) { %>
                        <tr>
                            <td><%= intern.get("internship_id") %></td>
                            <td><%= intern.get("role_title") %></td>
                            <td><%= intern.get("company_name") %></td>
                            <td><%= intern.get("min_cgpa") %></td>
                            <td><%= intern.get("deadline") %></td>
                            <td><span style="padding: 4px 8px; border-radius: 4px; font-size: 0.8em; font-weight: bold; background-color: <%= "OPEN".equals(intern.get("status")) ? "#dcfce7" : "#f1f5f9" %>; color: <%= "OPEN".equals(intern.get("status")) ? "#166534" : "#475569" %>"><%= intern.get("status") %></span></td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>

        <div class="card" style="margin-top: 24px;">
            <h2>Internship Applications</h2>
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Student Name</th>
                        <th>CGPA</th>
                        <th>Role</th>
                        <th>Company</th>
                        <th>Status</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Map<String, Object> app : applications) { %>
                        <tr>
                            <td><%= app.get("application_id") %></td>
                            <td><%= app.get("student_name") %></td>
                            <td><%= app.get("cgpa") %></td>
                            <td><%= app.get("role_title") %></td>
                            <td><%= app.get("company_name") %></td>
                            <td>
                                <span style="padding: 4px 8px; border-radius: 4px; font-size: 0.8em; font-weight: bold; 
                                    background-color: <%= "APPLIED".equals(app.get("status")) ? "#fef3c7" : 
                                                          "SHORTLISTED".equals(app.get("status")) ? "#e0e7ff" : 
                                                          "SELECTED".equals(app.get("status")) ? "#dcfce7" : "#fee2e2" %>; 
                                    color: <%= "APPLIED".equals(app.get("status")) ? "#92400e" : 
                                               "SHORTLISTED".equals(app.get("status")) ? "#3730a3" : 
                                               "SELECTED".equals(app.get("status")) ? "#166534" : "#991b1b" %>">
                                    <%= app.get("status") %>
                                </span>
                            </td>
                            <td>
                                <form action="${pageContext.request.contextPath}/admin/action" method="post" style="display: flex; gap: 10px;">
                                    <input type="hidden" name="action" value="updateApplicationStatus">
                                    <input type="hidden" name="applicationId" value="<%= app.get("application_id") %>">
                                    <select name="status" style="padding: 4px; border-radius: 4px;">
                                        <option value="APPLIED" <%= "APPLIED".equals(app.get("status")) ? "selected" : "" %>>APPLIED</option>
                                        <option value="SHORTLISTED" <%= "SHORTLISTED".equals(app.get("status")) ? "selected" : "" %>>SHORTLISTED</option>
                                        <option value="SELECTED" <%= "SELECTED".equals(app.get("status")) ? "selected" : "" %>>SELECTED</option>
                                        <option value="REJECTED" <%= "REJECTED".equals(app.get("status")) ? "selected" : "" %>>REJECTED</option>
                                    </select>
                                    <button type="submit" class="btn btn-primary" style="padding:4px 8px; font-size:0.875rem;">Update</button>
                                </form>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>

        <div class="card" style="margin-top: 24px;">
            <h2>Completed Exams (Subjective Evaluation)</h2>
            <table>
                <thead>
                    <tr>
                        <th>Attempt ID</th>
                        <th>Student Name</th>
                        <th>Exam Title</th>
                        <th>Auto-Graded MCQ Marks</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (completedExams.isEmpty()) { %>
                        <tr><td colspan="5">No completed exams waiting for evaluation.</td></tr>
                    <% } else { %>
                        <% for (Map<String, Object> exam : completedExams) { %>
                            <tr>
                                <td><%= exam.get("attempt_id") %></td>
                                <td><%= exam.get("student_name") %></td>
                                <td><%= exam.get("exam_title") %></td>
                                <td><%= exam.get("mcq_marks") %></td>
                                <td>
                                    <form action="${pageContext.request.contextPath}/evaluate_exam.jsp" method="get">
                                        <input type="hidden" name="attemptId" value="<%= exam.get("attempt_id") %>">
                                        <input type="hidden" name="applicationId" value="<%= exam.get("application_id") %>">
                                        <input type="hidden" name="studentName" value="<%= exam.get("student_name") %>">
                                        <input type="hidden" name="mcqMarks" value="<%= exam.get("mcq_marks") %>">
                                        <button type="submit" class="btn btn-primary" style="padding:4px 8px; font-size:0.875rem;">Evaluate Subjective</button>
                                    </form>
                                </td>
                            </tr>
                        <% } %>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>
</body>
</html>
