package com.institute.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(urlPatterns = {"/student/*", "/admin/*", "/exam/*"})
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic if any
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
            
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        
        String path = req.getRequestURI();

        // Check if session exists and user is logged in
        if (session == null || session.getAttribute("userId") == null) {
            res.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        // IP Binding Check
        String boundIp = (String) session.getAttribute("ipAddress");
        if (boundIp != null && !boundIp.equals(req.getRemoteAddr())) {
            session.invalidate();
            res.sendRedirect(req.getContextPath() + "/login.jsp?errorMessage=Session+invalidated+due+to+IP+change");
            return;
        }

        String role = (String) session.getAttribute("role");

        // Simple Role-Based Access Control
        if (path.contains("/admin/") && !"ADMIN".equals(role)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Admins Only");
            return;
        }
        
        if ((path.contains("/student/") || path.contains("/exam/")) && !"STUDENT".equals(role)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Students Only");
            return;
        }

        // Pass the request along the filter chain
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup logic if any
    }
}
