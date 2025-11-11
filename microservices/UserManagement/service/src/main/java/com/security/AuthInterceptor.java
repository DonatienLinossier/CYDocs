package com.example.demo.security;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.demo.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;

    public AuthInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod)) return true;

        HandlerMethod method = (HandlerMethod) handler;

        LoginRequired annotation = method.getMethodAnnotation(LoginRequired.class);
        if (annotation == null) return true;

        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty())
            token = request.getParameter("token");

        if (token == null || tokenService.validate(token, "LOGIN") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("❌ Token invalide ou expiré");
            return false;
        }

        return true;
    }
}
