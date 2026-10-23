package com.sports.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sports.common.dto.ErrorResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.DuplicateKeyException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;

/**
 * Tiered rate-limiting filter with:
 * - Per-endpoint limits (auth < ai < general)
 * - X-Forwarded-For spoofing protection (only trust header from known proxies)
 * - No internal path disclosure in error responses
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    @Value("${app.rate-limit.general-rpm:100}")
    private int generalRpm;

    @Value("${app.rate-limit.auth-rpm:10}")
    private int authRpm;

    @Value("${app.rate-limit.ai-rpm:20}")
    private int aiRpm;

    @Value("${app.security.trusted-proxy-ips:127.0.0.1,::1}")
    private String trustedProxyIpsRaw;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JdbcTemplate jdbcTemplate;
    private final AtomicLong lastCleanupWindow = new AtomicLong(-1);

    public RateLimitingFilter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = resolveClientIp(request);
        String path     = request.getRequestURI();

        // Determine which tier this request belongs to
        int limit;
        String tier;

        if (path.startsWith("/api/v1/auth/")) {
            limit = authRpm;
            tier = "auth";
        } else if (path.startsWith("/api/v1/ai-agent/")) {
            limit = aiRpm;
            tier = "ai";
        } else {
            limit = generalRpm;
            tier = "general";
        }

        long window = java.time.Instant.now().getEpochSecond() / 60;
        String bucket = hash(tier + ":" + clientIp);
        int count = incrementBucket(bucket, window);
        cleanupOldWindows(window);

        if (count > limit) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", "60");

            // No path disclosure in the error response
            ErrorResponse errorResponse = ErrorResponse.of(
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    "TOO_MANY_REQUESTS",
                    "تجاوزت الحد المسموح به من الطلبات. يرجى الانتظار دقيقة وتكرار المحاولة.",
                    null  // ← never expose the request path
            );

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private int incrementBucket(String bucket, long window) {
        try {
            return jdbcTemplate.queryForObject(
                    "UPDATE dbo.rate_limit_buckets WITH (ROWLOCK) SET request_count = request_count + 1 " +
                            "OUTPUT inserted.request_count WHERE bucket_key = ? AND window_start = ?",
                    Integer.class, bucket, window);
        } catch (EmptyResultDataAccessException missing) {
            try {
                jdbcTemplate.update("INSERT INTO dbo.rate_limit_buckets(bucket_key, window_start, request_count) VALUES (?, ?, 1)", bucket, window);
                return 1;
            } catch (DuplicateKeyException concurrentInsert) {
                return jdbcTemplate.queryForObject(
                        "UPDATE dbo.rate_limit_buckets WITH (ROWLOCK) SET request_count = request_count + 1 " +
                                "OUTPUT inserted.request_count WHERE bucket_key = ? AND window_start = ?",
                        Integer.class, bucket, window);
            }
        }
    }

    private void cleanupOldWindows(long window) {
        long current = window;
        if (lastCleanupWindow.getAndSet(current) != current) {
            jdbcTemplate.update("DELETE FROM dbo.rate_limit_buckets WHERE window_start < ?", window - 2);
        }
    }

    private String hash(String value) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    /**
     * Resolve client IP safely.
     * Only trusts X-Forwarded-For when the direct remote address is a known/trusted proxy.
     * Prevents IP-spoofing via crafted X-Forwarded-For headers from untrusted clients.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();

        Set<String> trustedProxies = Arrays.stream(trustedProxyIpsRaw.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        if (trustedProxies.contains(remoteAddr)) {
            String xfHeader = request.getHeader("X-Forwarded-For");
            if (xfHeader != null && !xfHeader.isBlank()) {
                // Take the first (leftmost) IP — the original client
                String firstIp = xfHeader.split(",")[0].trim();
                if (!firstIp.isBlank()) {
                    return firstIp;
                }
            }
        }

        return remoteAddr;
    }
}
