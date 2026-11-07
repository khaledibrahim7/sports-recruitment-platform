package com.sports.web.service;

import com.sports.auth.domain.RefreshSession;
import com.sports.auth.domain.User;
import com.sports.auth.repository.RefreshSessionRepository;
import com.sports.auth.repository.UserRepository;
import com.sports.common.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class RefreshSessionService {
    private final RefreshSessionRepository sessions;
    private final UserRepository users;
    private final UserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.auth.refresh-ttl-seconds:2592000}")
    private long refreshTtlSeconds;

    public RefreshSessionService(RefreshSessionRepository sessions, UserRepository users,
                                 UserDetailsService userDetailsService, JwtTokenProvider jwtTokenProvider) {
        this.sessions = sessions;
        this.users = users;
        this.userDetailsService = userDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public SessionTokens create(Long userId, String userAgent) {
        sessions.deleteByExpiresAtBefore(Instant.now());
        User user = users.findById(userId).orElseThrow(() -> unauthorized());
        if (!isUserEnabled(user)) throw unauthorized();
        String refreshToken = newRefreshToken();
        Instant now = Instant.now();
        sessions.save(new RefreshSession(userId, hash(refreshToken), now, now.plusSeconds(refreshTtlSeconds), safeUserAgent(userAgent)));
        return tokensFor(user, refreshToken);
    }

    @Transactional
    public SessionTokens rotate(String refreshToken, String userAgent) {
        if (refreshToken == null || refreshToken.isBlank()) throw unauthorized();
        RefreshSession old = sessions.findByTokenHashForUpdate(hash(refreshToken)).orElseThrow(RefreshSessionService::unauthorized);
        Instant now = Instant.now();
        if (!old.getExpiresAt().isAfter(now)) {
            sessions.delete(old);
            throw unauthorized();
        }
        User user = users.findById(old.getUserId()).orElseThrow(RefreshSessionService::unauthorized);
        if (!isUserEnabled(user)) {
            sessions.delete(old);
            throw unauthorized();
        }
        sessions.delete(old);
        sessions.flush();
        String nextRefresh = newRefreshToken();
        sessions.save(new RefreshSession(user.getId(), hash(nextRefresh), now, now.plusSeconds(refreshTtlSeconds), safeUserAgent(userAgent)));
        return tokensFor(user, nextRefresh);
    }

    @Transactional
    public void revoke(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) sessions.deleteByTokenHash(hash(refreshToken));
    }

    private SessionTokens tokensFor(User user, String refreshToken) {
        UserDetails details = userDetailsService.loadUserByUsername(user.getEmail());
        var authentication = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        return new SessionTokens(jwtTokenProvider.generateToken(authentication), refreshToken,
                user.getId(), user.getEmail(), user.getFullName(), user.getRole().name());
    }

    private static boolean isUserEnabled(User user) {
        return user.isEmailVerified() && user.isGuardianConsentVerified()
                && (user.getRole() == com.sports.auth.domain.Role.PLAYER || user.isOrganizationApproved());
    }

    private String newRefreshToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }

    private static String safeUserAgent(String userAgent) {
        if (userAgent == null) return null;
        String safe = userAgent.replaceAll("[\\r\\n\\p{Cntrl}]", " ").trim();
        return safe.length() > 512 ? safe.substring(0, 512) : safe;
    }

    private static ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired or invalid");
    }

    public record SessionTokens(String accessToken, String refreshToken, Long userId,
                                String email, String fullName, String role) {}
}
