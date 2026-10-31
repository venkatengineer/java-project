package com.examination.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            for (GrantedAuthority authority : auth.getAuthorities()) {
                String role = authority.getAuthority();
                if ("ROLE_ADMIN".equals(role)) {
                    response.sendRedirect(request.getContextPath() + "/admin/dashboard?accessDenied=true");
                    return;
                } else if ("ROLE_TEACHER".equals(role)) {
                    response.sendRedirect(request.getContextPath() + "/teacher/dashboard?accessDenied=true");
                    return;
                } else if ("ROLE_STUDENT".equals(role)) {
                    response.sendRedirect(request.getContextPath() + "/student/dashboard?accessDenied=true");
                    return;
                }
            }
        }
        response.sendRedirect(request.getContextPath() + "/login?accessDenied=true");
    }
}
