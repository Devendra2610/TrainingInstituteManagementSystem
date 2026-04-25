package com.institute.controller;

import com.institute.dao.InternshipDAO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/student/apply")
public class ApplicationServlet extends HttpServlet {

    private InternshipDAO internshipDAO;

    @Override
    public void init() throws ServletException {
        internshipDAO = new InternshipDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        int studentId = (Integer) session.getAttribute("userId");
        int internshipId = Integer.parseInt(request.getParameter("internshipId"));

        // 1. Prevent duplicate applications
        if (internshipDAO.hasApplied(studentId, internshipId)) {
            response.getWriter().write("You have already applied for this internship.");
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return;
        }

        // 2. Process application with transaction
        boolean success = internshipDAO.applyForInternship(studentId, internshipId);

        if (success) {
            response.getWriter().write("Successfully applied for the internship!");
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.getWriter().write("Application failed. Please try again later.");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
