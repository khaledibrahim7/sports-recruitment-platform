package com.sports.auth.service;

import com.sports.auth.domain.User;
import com.sports.auth.dto.AuthResponse;
import com.sports.auth.dto.LoginRequest;
import com.sports.auth.dto.RegisterRequest;
import com.sports.auth.repository.UserRepository;
import com.sports.common.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sports.auth.domain.Role;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.isTermsAccepted()) {
            throw new IllegalArgumentException("Terms and privacy consent is required");
        }
        String normalizedEmail = request.getEmail().trim().toLowerCase(java.util.Locale.ROOT);
        request.setEmail(normalizedEmail);
        if (request.getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must be no longer than 72 UTF-8 bytes");
        }
        if (userRepository.existsByEmail(normalizedEmail)) {
            return new AuthResponse(null, null, normalizedEmail, "", request.getRole());
        }

        String name = (request.getFullName() != null && !request.getFullName().isBlank())
                ? request.getFullName().trim()
                : request.getEmail().split("@")[0];

        User user = new User(
                normalizedEmail,
                passwordEncoder.encode(request.getPassword()),
                request.getRole(),
                name
        );
        user.setTermsAccepted(request.isTermsAccepted());
        user.setTermsVersion("2026-10");
        user.setTermsAcceptedAt(java.time.Instant.now());
        user.setEmailVerified(false);
        user.setOrganizationApproved(request.getRole() == Role.PLAYER);
        RegisterRequest.PlayerRegistration player = request.getPlayerProfileForValidation();
        boolean minor = request.getRole() == Role.PLAYER && player != null && player.getBirthDate() != null
                && java.time.Period.between(player.getBirthDate(), java.time.LocalDate.now()).getYears() < 18;
        user.setGuardianConsentVerified(!minor);

        User savedUser = userRepository.save(user);

        return new AuthResponse(null, savedUser.getId(), savedUser.getEmail(), savedUser.getFullName(), savedUser.getRole());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        request.setEmail(request.getEmail().trim().toLowerCase(java.util.Locale.ROOT));
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new AuthResponse(token, user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }
}
