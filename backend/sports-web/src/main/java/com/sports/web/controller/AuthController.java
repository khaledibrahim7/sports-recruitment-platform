package com.sports.web.controller;

import com.sports.auth.dto.AuthResponse;
import com.sports.auth.dto.LoginRequest;
import com.sports.auth.dto.RegisterRequest;
import com.sports.auth.domain.VerificationToken;
import com.sports.auth.service.AuthService;
import com.sports.common.dto.ApiResponse;
import com.sports.web.service.AccountVerificationService;
import com.sports.web.service.RegistrationWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication & Accounts", description = "Authentication and registration endpoints")
public class AuthController {
    private static final String REGISTRATION_MESSAGE = "If this address can be registered, confirmation instructions will be sent.";
    private final AuthService authService;
    private final RegistrationWorkflowService registrationWorkflow;
    private final AccountVerificationService verificationService;

    public AuthController(AuthService authService, RegistrationWorkflowService registrationWorkflow,
                          AccountVerificationService verificationService) {
        this.authService = authService;
        this.registrationWorkflow = registrationWorkflow;
        this.verificationService = verificationService;
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
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Login successful", authService.login(request)));
    }
}
