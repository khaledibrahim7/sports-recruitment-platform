package com.sports.web.controller;

import com.sports.auth.dto.AuthResponse;
import com.sports.auth.dto.LoginRequest;
import com.sports.auth.dto.RegisterRequest;
import com.sports.auth.domain.VerificationToken;
import com.sports.auth.service.AuthService;
import com.sports.common.dto.ApiResponse;
import com.sports.web.service.AccountVerificationService;
import com.sports.web.service.RegistrationWorkflowService;
import com.sports.web.service.RefreshSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication & Accounts", description = "Authentication and registration endpoints")
public class AuthController {
    private static final String REGISTRATION_MESSAGE = "If this address can be registered, confirmation instructions will be sent.";
    private final AuthService authService;
    private final RegistrationWorkflowService registrationWorkflow;
    private final AccountVerificationService verificationService;
    private final RefreshSessionService refreshSessionService;

    @Value("${app.security.allowed-origins:http://localhost:4200}")
    private String allowedOriginsRaw;
    @Value("${app.auth.cookie-secure:false}")
    private boolean secureCookies;
    @Value("${app.jwt.expiration-ms:600000}")
    private long accessTtlMillis;
    @Value("${app.auth.refresh-ttl-seconds:2592000}")
    private long refreshTtlSeconds;

    public AuthController(AuthService authService, RegistrationWorkflowService registrationWorkflow,
                          AccountVerificationService verificationService, RefreshSessionService refreshSessionService) {
        this.authService = authService;
        this.registrationWorkflow = registrationWorkflow;
        this.verificationService = verificationService;
        this.refreshSessionService = refreshSessionService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register an account and send required verification instructions")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        registrationWorkflow.register(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new ApiResponse<>(true, HttpStatus.ACCEPTED.value(), REGISTRATION_MESSAGE, null));
    }

    public record VerificationRequest(@jakarta.validation.constraints.NotBlank String token) {}
    public record ResendVerificationRequest(
            @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Email
            @jakarta.validation.constraints.Size(max = 150) String email) {}

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        verificationService.resend(request.email());
        return ResponseEntity.ok(ApiResponse.ok("If the account needs confirmation, a new link will be sent when the cooldown allows it.", null));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerificationRequest request) {
        verificationService.verify(request.token(), VerificationToken.Type.EMAIL);
        return ResponseEntity.ok(ApiResponse.ok("Email verified. You can now sign in when any required approvals are complete.", null));
    }

    @PostMapping("/verify-guardian")
    public ResponseEntity<ApiResponse<Void>> verifyGuardian(@Valid @RequestBody VerificationRequest request) {
        verificationService.verify(request.token(), VerificationToken.Type.GUARDIAN);
        return ResponseEntity.ok(ApiResponse.ok("Guardian consent verified.", null));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user credentials and return JWT token")
    public ResponseEntity<ApiResponse<SessionUser>> login(@Valid @RequestBody LoginRequest request,
                                                          HttpServletRequest servletRequest) {
        AuthResponse authenticated = authService.login(request);
        RefreshSessionService.SessionTokens tokens = refreshSessionService.create(
                authenticated.getUserId(), servletRequest.getHeader("User-Agent"));
        HttpHeaders headers = sessionCookies(tokens);
        return ResponseEntity.ok().headers(headers).body(ApiResponse.ok("Login successful", SessionUser.from(tokens)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<SessionUser>> refresh(
            @CookieValue(name = "REFRESH_TOKEN", required = false) String refreshToken,
            @RequestHeader(value = "Origin", required = false) String origin,
            HttpServletRequest servletRequest) {
        requireTrustedOrigin(origin);
        RefreshSessionService.SessionTokens tokens = refreshSessionService.rotate(refreshToken, servletRequest.getHeader("User-Agent"));
        return ResponseEntity.ok().headers(sessionCookies(tokens))
                .body(ApiResponse.ok("Session refreshed", SessionUser.from(tokens)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "REFRESH_TOKEN", required = false) String refreshToken,
            @RequestHeader(value = "Origin", required = false) String origin) {
        requireTrustedOrigin(origin);
        refreshSessionService.revoke(refreshToken);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, expiredCookie("ACCESS_TOKEN", "/api/v1").toString());
        headers.add(HttpHeaders.SET_COOKIE, expiredCookie("REFRESH_TOKEN", "/api/v1/auth").toString());
        return ResponseEntity.noContent().headers(headers).build();
    }

    private HttpHeaders sessionCookies(RefreshSessionService.SessionTokens tokens) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, ResponseCookie.from("ACCESS_TOKEN", tokens.accessToken())
                .httpOnly(true).secure(secureCookies).sameSite("Lax").path("/api/v1")
                .maxAge(Duration.ofMillis(accessTtlMillis)).build().toString());
        headers.add(HttpHeaders.SET_COOKIE, ResponseCookie.from("REFRESH_TOKEN", tokens.refreshToken())
                .httpOnly(true).secure(secureCookies).sameSite("Lax").path("/api/v1/auth")
                .maxAge(Duration.ofSeconds(refreshTtlSeconds)).build().toString());
        return headers;
    }

    private ResponseCookie expiredCookie(String name, String path) {
        return ResponseCookie.from(name, "").httpOnly(true).secure(secureCookies).sameSite("Lax")
                .path(path).maxAge(Duration.ZERO).build();
    }

    private void requireTrustedOrigin(String origin) {
        List<String> allowed = Arrays.stream(allowedOriginsRaw.split(",")).map(String::trim).toList();
        if (origin == null || !allowed.contains(origin)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Untrusted request origin");
        }
    }

    public record SessionUser(Long userId, String email, String fullName, String role) {
        static SessionUser from(RefreshSessionService.SessionTokens tokens) {
            return new SessionUser(tokens.userId(), tokens.email(), tokens.fullName(), tokens.role());
        }
    }
}
