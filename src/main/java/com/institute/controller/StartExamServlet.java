package com.institute.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/student/startExam")
public class StartExamServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || !"STUDENT".equals(session.getAttribute("role"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Initialize exam details
        session.setAttribute("examId", 1); // Mock exam ID
        session.setAttribute("examStartTime", System.currentTimeMillis());
        session.setAttribute("examDuration", 60); // 60 minutes
        
        response.sendRedirect(request.getContextPath() + "/exam_interface.jsp");
    }
}
