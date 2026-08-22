package com.alzain.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();
    private static final long TIME_WINDOW_MS = 60000; // 1 minute window

    private static class RequestCounter {
        final long windowStart;
        final AtomicInteger count;

        RequestCounter(long windowStart) {
            this.windowStart = windowStart;
            this.count = new AtomicInteger(1);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        int maxAllowed = getMaxAllowedRequests(path, method);
        if (maxAllowed > 0) {
            String clientIp = getClientIP(request);
            String key = clientIp + ":" + path;

            long currentTime = System.currentTimeMillis();

            RequestCounter counter = requestCounts.compute(key, (k, existing) -> {
                if (existing == null || (currentTime - existing.windowStart) > TIME_WINDOW_MS) {
                    return new RequestCounter(currentTime);
                } else {
                    existing.count.incrementAndGet();
                    return existing;
                }
            });

            if (counter.count.get() > maxAllowed) {
                log.warn("Rate limit exceeded for IP {} on endpoint {} (Count: {})", clientIp, path, counter.count.get());
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Please try again after 1 minute.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private int getMaxAllowedRequests(String path, String method) {
        if ("POST".equalsIgnoreCase(method)) {
            if (path.startsWith("/api/auth/admin/login") || path.startsWith("/api/auth/login")) {
                return 10; // 10 login attempts / min
            }
            if (path.startsWith("/api/auth/admin/forgot-password")) {
                return 5; // 5 reset requests / min
            }
            if (path.startsWith("/api/public/bookings")) {
                return 10; // 10 bookings / min
            }
            if (path.startsWith("/api/public/contact")) {
                return 5; // 5 contact enquiries / min
            }
            if (path.startsWith("/api/public/reviews")) {
                return 5; // 5 review submissions / min
            }
        }
        return 0; // Unlimited for other endpoints
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
