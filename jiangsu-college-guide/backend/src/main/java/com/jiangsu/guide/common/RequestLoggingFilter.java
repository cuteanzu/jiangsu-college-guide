package com.jiangsu.guide.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String client = request.getHeader("X-Forwarded-For");
        if (client == null) client = request.getRemoteAddr();

        filterChain.doFilter(request, response);

        long duration = System.currentTimeMillis() - start;
        int status = response.getStatus();
        String path = query != null ? uri + "?" + query : uri;

        if (status >= 400) {
            log.warn("{} {} → {} ({}ms) [{}]", method, path, status, duration, client);
        } else {
            log.info("{} {} → {} ({}ms)", method, path, status, duration);
        }
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // 跳过静态资源和 H2 控制台
        String path = request.getRequestURI();
        return path.startsWith("/h2-console") || path.contains(".");
    }
}
