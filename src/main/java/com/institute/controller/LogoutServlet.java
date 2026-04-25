package com.institute.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            
        HttpSession session = request.getSession(false);
        if (session != null) {
            // This invalidate() call triggers the ExamSessionListener
            // which handles auto-submission of exams and resetting the is_logged_in flag in the DB.
            session.invalidate(); 
        }
        
        response.sendRedirect(request.getContextPath() + "/login.jsp?msg=You+have+been+logged+out+successfully");
    }
}
