package com.sports.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sports.common.dto.ErrorResponse;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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

    // Separate caches per tier to allow different limits
    private final Cache<String, AtomicInteger> generalCache = buildCache();
    private final Cache<String, AtomicInteger> authCache    = buildCache();
    private final Cache<String, AtomicInteger> aiCache      = buildCache();

    private static Cache<String, AtomicInteger> buildCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
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
        Cache<String, AtomicInteger> cache;

        if (path.startsWith("/api/v1/auth/")) {
            limit = authRpm;
            cache = authCache;
        } else if (path.startsWith("/api/v1/ai-agent/")) {
            limit = aiRpm;
            cache = aiCache;
        } else {
            limit = generalRpm;
            cache = generalCache;
        }

        AtomicInteger counter = cache.get(clientIp, k -> new AtomicInteger(0));

        if (counter.incrementAndGet() > limit) {
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
