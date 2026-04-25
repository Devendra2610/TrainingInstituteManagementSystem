package com.institute.controller;

import com.institute.dao.CompanyDAO;
import com.institute.dao.InternshipDAO;
import com.institute.dao.ApplicationDAO;
import com.institute.dao.ExamDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/admin/action")
public class AdminActionServlet extends HttpServlet {

    private CompanyDAO companyDAO;
    private InternshipDAO internshipDAO;
    private ApplicationDAO applicationDAO;
    private ExamDAO examDAO;

    @Override
    public void init() throws ServletException {
        companyDAO = new CompanyDAO();
        internshipDAO = new InternshipDAO();
        applicationDAO = new ApplicationDAO();
        examDAO = new ExamDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("role"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            response.sendRedirect(request.getContextPath() + "/admin_reports.jsp");
            return;
        }

        try {
            if ("addCompany".equals(action)) {
                String companyName = request.getParameter("companyName");
                String industry = request.getParameter("industry");
                String website = request.getParameter("website");
                String username = request.getParameter("username");
                String password = request.getParameter("password");
                String email = request.getParameter("email");

                boolean success = companyDAO.addCompany(username, password, email, companyName, industry, website);
                if (success) {
                    response.sendRedirect(request.getContextPath() + "/admin_reports.jsp?msg=Company+added+successfully");
                } else {
                    response.sendRedirect(request.getContextPath() + "/admin_reports.jsp?err=Failed+to+add+company.+Username+or+email+might+already+exist.");
                }

            } else if ("deleteCompany".equals(action)) {
                int companyId = Integer.parseInt(request.getParameter("companyId"));
                boolean success = companyDAO.deleteCompany(companyId);
                if (success) {
                    response.sendRedirect(request.getContextPath() + "/admin_reports.jsp?msg=Company+deleted+successfully");
                } else {
                    response.sendRedirect(request.getContextPath() + "/admin_reports.jsp?err=Failed+to+delete+company");
                }

            } else if ("addInternship".equals(action)) {
                int companyId = Integer.parseInt(request.getParameter("companyId"));
                String roleTitle = request.getParameter("roleTitle");
                String description = request.getParameter("description");
                String stipendStr = request.getParameter("stipend");
                double stipend = (stipendStr != null && !stipendStr.isEmpty()) ? Double.parseDouble(stipendStr) : 0.0;
                double minCgpa = Double.parseDouble(request.getParameter("minCgpa"));
                String deadline = request.getParameter("deadline");

                boolean success = internshipDAO.addInternship(companyId, roleTitle, description, stipend, minCgpa, deadline);
                if (success) {
                    response.sendRedirect(request.getContextPath() + "/admin_reports.jsp?msg=Internship+posted+successfully");
                } else {
                    response.sendRedirect(request.getContextPath() + "/admin_reports.jsp?err=Failed+to+post+internship");
                }
            } else if ("updateApplicationStatus".equals(action)) {
                int applicationId = Integer.parseInt(request.getParameter("applicationId"));
                String status = request.getParameter("status");
                
                boolean success = applicationDAO.updateApplicationStatus(applicationId, status);
                if (success) {
                    response.sendRedirect(request.getContextPath() + "/admin_reports.jsp?msg=Application+status+updated+successfully");
                } else {
                    response.sendRedirect(request.getContextPath() + "/admin_reports.jsp?err=Failed+to+update+application+status");
                }
            } else if ("submitEvaluation".equals(action)) {
                int attemptId = Integer.parseInt(request.getParameter("attemptId"));
                int applicationId = Integer.parseInt(request.getParameter("applicationId"));
                String finalStatus = request.getParameter("finalStatus"); // 'SELECTED' or 'REJECTED'
                
                java.util.Map<Integer, Integer> subjectiveMarks = new java.util.HashMap<>();
                java.util.Enumeration<String> params = request.getParameterNames();
                while (params.hasMoreElements()) {
                    String paramName = params.nextElement();
                    if (paramName.startsWith("mark_")) {
                        int answerId = Integer.parseInt(paramName.substring(5));
                        int mark = Integer.parseInt(request.getParameter(paramName));
                        subjectiveMarks.put(answerId, mark);
                    }
                }
                
                boolean success = examDAO.finalizeEvaluation(attemptId, applicationId, subjectiveMarks, finalStatus);
                if (success) {
                    response.sendRedirect(request.getContextPath() + "/admin_reports.jsp?msg=Exam+evaluation+completed+and+result+finalized!");
                } else {
                    response.sendRedirect(request.getContextPath() + "/admin_reports.jsp?err=Failed+to+save+evaluation");
                }
            } else {
                response.sendRedirect(request.getContextPath() + "/admin_reports.jsp");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin_reports.jsp?err=An+unexpected+error+occurred");
        }
    }
}
