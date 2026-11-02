package com.sports.web.config;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/** Fail fast when registration or authentication cannot operate safely outside local/test profiles. */
@Configuration
public class ProductionAuthConfiguration {

    @Bean
    InitializingBean validateProductionAuthSettings(Environment environment) {
        return () -> {
            boolean localProfile = environment.acceptsProfiles(Profiles.of("local"));
            boolean productionProfile = environment.acceptsProfiles(Profiles.of("prod", "production"));
            boolean testProfile = environment.acceptsProfiles(Profiles.of("test"));
            if (localProfile && productionProfile) {
                throw new IllegalStateException("The local profile cannot be combined with a production profile");
            }
            if (localProfile || (testProfile && !productionProfile)) return;

            String smtpHost = required(environment, "spring.mail.host");
            String smtpUsername = required(environment, "spring.mail.username");
            String smtpPassword = required(environment, "spring.mail.password");
            String jwtSecret = required(environment, "app.jwt.secret");
            String publicUrl = required(environment, "app.public-url");
            String allowedOrigins = required(environment, "app.security.allowed-origins");

            if (smtpHost.length() > 253 || smtpUsername.length() > 254 || smtpPassword.length() < 8) {
                throw new IllegalStateException("Production SMTP settings are invalid");
            }
            if (!environment.getProperty("spring.mail.properties.mail.smtp.auth", Boolean.class, true)
                    || !environment.getProperty("spring.mail.properties.mail.smtp.starttls.enable", Boolean.class, true)) {
                throw new IllegalStateException("Production SMTP authentication and STARTTLS must be enabled");
            }
            if (environment.getProperty("spring.mail.port", Integer.class, 587) < 1
                    || environment.getProperty("spring.mail.port", Integer.class, 587) > 65535) {
                throw new IllegalStateException("Production SMTP port is invalid");
            }

            String mailFrom = required(environment, "app.mail.from");
            try {
                InternetAddress sender = new InternetAddress(mailFrom, true);
                sender.validate();
            } catch (AddressException invalidAddress) {
                throw new IllegalStateException("MAIL_FROM must be a valid sender email address");
            }

            if (jwtSecret.getBytes(StandardCharsets.UTF_8).length < 32
                    || jwtSecret.chars().distinct().count() < 16
                    || jwtSecret.toLowerCase(java.util.Locale.ROOT).contains("localdev")
                    || jwtSecret.toLowerCase(java.util.Locale.ROOT).contains("minimumthirtytwo")) {
                throw new IllegalStateException("JWT_SECRET must be a unique secret of at least 32 bytes");
            }

            URI publicUri = parseUri(publicUrl, "APP_PUBLIC_URL");
            if (!"https".equalsIgnoreCase(publicUri.getScheme()) || publicUri.getHost() == null
                    || publicUri.getRawQuery() != null || publicUri.getRawFragment() != null) {
                throw new IllegalStateException("APP_PUBLIC_URL must be a public HTTPS URL without query or fragment");
            }
            String publicOrigin = publicUri.getScheme() + "://" + publicUri.getRawAuthority();
            List<String> origins = Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .toList();
            if (origins.isEmpty() || origins.stream().noneMatch(origin -> sameHttpsOrigin(origin, publicOrigin))) {
                throw new IllegalStateException("ALLOWED_ORIGINS must include the HTTPS origin of APP_PUBLIC_URL");
            }
            if (origins.stream().anyMatch(origin -> origin.contains("localhost") || origin.contains("127.0.0.1"))) {
                throw new IllegalStateException("Production ALLOWED_ORIGINS cannot contain local development origins");
            }

            java.util.Set<String> trustedProxies = Arrays.stream(environment
                            .getProperty("app.security.trusted-proxy-ips", "127.0.0.1,::1").split(","))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .collect(java.util.stream.Collectors.toSet());
            if (trustedProxies.isEmpty() || trustedProxies.equals(java.util.Set.of("127.0.0.1", "::1"))) {
                throw new IllegalStateException("TRUSTED_PROXY_IPS must contain the production ingress proxy addresses");
            }
        };
    }

    private static String required(Environment environment, String key) {
        String value = environment.getProperty(key, "").trim();
        if (value.isEmpty()) {
            throw new IllegalStateException("Required production setting is missing: " + key);
        }
        return value;
    }

    private static URI parseUri(String value, String settingName) {
        try {
            return URI.create(value);
        } catch (IllegalArgumentException invalidUri) {
            throw new IllegalStateException(settingName + " must be a valid URL");
        }
    }

    private static boolean sameHttpsOrigin(String candidate, String expectedOrigin) {
        try {
            URI uri = URI.create(candidate);
            return "https".equalsIgnoreCase(uri.getScheme())
                    && uri.getHost() != null
                    && (uri.getRawPath() == null || uri.getRawPath().isEmpty() || "/".equals(uri.getRawPath()))
                    && uri.getRawQuery() == null
                    && uri.getRawFragment() == null
                    && expectedOrigin.equalsIgnoreCase(uri.getScheme() + "://" + uri.getRawAuthority());
        } catch (IllegalArgumentException invalidUri) {
            return false;
        }
    }
}
