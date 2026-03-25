package com.example.demo.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Component
@Order(1)
public class JwtUserIdFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            String userId = jwtAuth.getToken().getSubject();
            chain.doFilter(new UserIdRequestWrapper((HttpServletRequest) request, userId), response);
        } else {
            chain.doFilter(request, response);
        }
    }

    private static class UserIdRequestWrapper extends HttpServletRequestWrapper {

        private final String userId;

        public UserIdRequestWrapper(HttpServletRequest request, String userId) {
            super(request);
            this.userId = userId;
        }

        @Override
        public String getHeader(String name) {
            if ("X-User-Id".equalsIgnoreCase(name)) return userId;
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if ("X-User-Id".equalsIgnoreCase(name)) {
                return Collections.enumeration(List.of(userId));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> names = Collections.list(super.getHeaderNames());
            if (!names.contains("x-user-id")) {
                names.add("X-User-Id");
            }
            return Collections.enumeration(names);
        }
    }
}
