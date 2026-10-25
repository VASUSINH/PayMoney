
package com.PayMoney.Service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_SECONDS = 60;

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT;

    static {
        RATE_LIMIT_SCRIPT = new DefaultRedisScript<>();
        RATE_LIMIT_SCRIPT.setScriptText(
                "local count = redis.call('INCR', KEYS[1]) " +
                        "if count == 1 then " +
                        "redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
                        "end " +
                        "return count"
        );
        RATE_LIMIT_SCRIPT.setResultType(Long.class);
    }

    private final StringRedisTemplate redisTemplate;

    public RateLimitFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String group = getRateLimitGroup(request);

        if (group == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String identity = getClientIdentity(request, group);
        String redisKey = "ratelimit:" + group + ":" + identity;

        Long requestCount;

        try {
            requestCount = redisTemplate.execute(
                    RATE_LIMIT_SCRIPT,
                    List.of(redisKey),
                    String.valueOf(WINDOW_SECONDS)
            );
        } catch (RuntimeException exception) {
            writeError(
                    response,
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Rate limiting service unavailable"
            );
            return;
        }

        if (requestCount == null) {
            writeError(
                    response,
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Rate limiting service unavailable"
            );
            return;
        }

        int limit = getLimit(group);

        if (requestCount > limit) {
            response.setHeader("Retry-After", String.valueOf(WINDOW_SECONDS));
            writeError(
                    response,
                    429,
                    "Too many requests. Please try again later."
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getRateLimitGroup(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getServletPath();

        if ("POST".equalsIgnoreCase(method)) {
            if ("/api/v1/auth/login".equals(path)) {
                return "login";
            }

            if ("/api/v1/register-user".equals(path)) {
                return "register";
            }

            if ("/api/v1/auth/reactivate".equals(path)) {
                return "reactivate";
            }

            if ("/api/transactions/transfer".equals(path)) {
                return "transfer";
            }

            if ("/api/wallet/deposit".equals(path)) {
                return "deposit";
            }

            if ("/api/wallet/withdraw".equals(path)) {
                return "withdraw";
            }

            if (path.startsWith("/api/payments/")) {
                return "payments";
            }
        }

        if ("PATCH".equalsIgnoreCase(method)
                && "/api/v1/users/me/deactivate".equals(path)) {
            return "deactivate";
        }

        return null;
    }

    private String getClientIdentity(
            HttpServletRequest request,
            String group
    ) {
        if ("login".equals(group)
                || "register".equals(group)
                || "reactivate".equals(group)) {
            return "ip:" + request.getRemoteAddr();
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getName() != null
                && !"anonymousUser".equals(authentication.getName())) {
            return "user:" + authentication.getName();
        }

        return "ip:" + request.getRemoteAddr();
    }

    private int getLimit(String group) {
        return switch (group) {
            case "login", "register", "reactivate", "deactivate" -> 5;
            case "transfer", "deposit", "withdraw", "payments" -> 10;
            default -> 10;
        };
    }

    private void writeError(
            HttpServletResponse response,
            int status,
            String message
    ) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"status\":" + status + ",\"error\":\"" + message + "\"}"
        );
    }
}
