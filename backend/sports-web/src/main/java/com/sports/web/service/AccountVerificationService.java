package com.sports.web.service;

import com.sports.auth.domain.User;
import com.sports.auth.domain.VerificationToken;
import com.sports.auth.repository.UserRepository;
import com.sports.auth.repository.VerificationTokenRepository;
import com.sports.player.repository.PlayerProfileRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
public class AccountVerificationService {
    private final VerificationTokenRepository tokens;
    private final UserRepository users;
    private final PlayerProfileRepository players;
    private final ObjectProvider<JavaMailSender> mailSenders;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.mail.from:no-reply@sports-recruitment.local}")
    private String from;
    @Value("${app.public-url:http://localhost:4200}")
    private String publicUrl;

    public AccountVerificationService(VerificationTokenRepository tokens, UserRepository users, PlayerProfileRepository players,
                                      ObjectProvider<JavaMailSender> mailSenders) {
        this.tokens = tokens;
        this.users = users;
        this.players = players;
        this.mailSenders = mailSenders;
    }

    @Transactional
    public void sendRegistrationEmails(User user, String guardianEmail) {
        send(user, user.getEmail(), VerificationToken.Type.EMAIL);
        if (guardianEmail != null && !guardianEmail.isBlank()) {
            send(user, guardianEmail.trim().toLowerCase(java.util.Locale.ROOT), VerificationToken.Type.GUARDIAN);
        }
    }

    @Transactional
    public void resend(String email) {
        User user = users.findByEmail(email.trim().toLowerCase(java.util.Locale.ROOT)).orElse(null);
        if (user == null) return;
        Instant now = Instant.now();
        if (!user.isEmailVerified() && canResend(user.getVerificationEmailSentAt(), now)) {
            send(user, user.getEmail(), VerificationToken.Type.EMAIL);
        }
        if (!user.isGuardianConsentVerified() && canResend(user.getGuardianConsentEmailSentAt(), now)) {
            tokens.findTopByUser_IdAndTypeOrderByIdDesc(user.getId(), VerificationToken.Type.GUARDIAN)
                    .ifPresent(token -> send(user, token.getDestinationEmail(), VerificationToken.Type.GUARDIAN));
        }
    }

    private boolean canResend(Instant lastSent, Instant now) {
        return lastSent == null || lastSent.plus(1, ChronoUnit.MINUTES).isBefore(now);
    }

    private void send(User user, String destination, VerificationToken.Type type) {
        JavaMailSender sender = mailSenders.getIfAvailable();
        if (sender == null) {
            throw new IllegalStateException("SMTP email delivery is not configured");
        }
        tokens.deleteByUser_IdAndType(user.getId(), type);
        String raw = newToken();
        tokens.save(new VerificationToken(user, hash(raw), type, destination, Instant.now().plus(24, ChronoUnit.HOURS)));
        String kind = type == VerificationToken.Type.EMAIL ? "email" : "guardian";
        String link = publicUrl.replaceAll("/$", "") + "/#/verify-account?kind=" + kind + "&token=" + raw;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(destination);
        message.setSubject(type == VerificationToken.Type.EMAIL ? "Confirm your Sports Recruitment account" : "Approve a minor's account");
        message.setText(type == VerificationToken.Type.EMAIL
                ? "Confirm your email address using this one-time link (expires in 24 hours):\n" + link
                : "A player under 18 requested an account. If you are their parent or legal guardian, review the platform's privacy terms and confirm permission for the player's account using this one-time link (expires in 24 hours):\n" + link);
        sender.send(message);
        Instant sentAt = Instant.now();
        if (type == VerificationToken.Type.EMAIL) user.setVerificationEmailSentAt(sentAt);
        else user.setGuardianConsentEmailSentAt(sentAt);
    }

    @Transactional
    public void verify(String rawToken, VerificationToken.Type type) {
        if (rawToken == null || rawToken.length() < 40 || rawToken.length() > 100) {
            throw new IllegalArgumentException("Verification link is invalid or expired");
        }
        VerificationToken token = tokens.findValidForUpdate(hash(rawToken), type, Instant.now())
                .orElseThrow(() -> new IllegalArgumentException("Verification link is invalid or expired"));
        token.setConsumedAt(Instant.now());
        User user = users.findById(token.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("Verification link is invalid or expired"));
        if (type == VerificationToken.Type.EMAIL) user.setEmailVerified(true);
        else {
            user.setGuardianConsentVerified(true);
            user.setGuardianConsentVerifiedAt(Instant.now());
            players.findByUserId(user.getId()).ifPresent(profile -> profile.setGuardianConsentAt(user.getGuardianConsentVerifiedAt()));
        }
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
