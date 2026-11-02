package com.sports.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/** Local-only mail sink. Never activate the local profile in production. */
@Configuration
@Profile("local")
public class LocalMailConfiguration {
    private static final Logger log = LoggerFactory.getLogger(LocalMailConfiguration.class);

    @Bean
    InitializingBean validateLocalSmtpSettings(Environment environment) {
        return () -> {
            boolean host = hasValue(environment, "spring.mail.host");
            boolean username = hasValue(environment, "spring.mail.username");
            boolean password = hasValue(environment, "spring.mail.password");
            boolean from = hasValue(environment, "app.mail.from");
            boolean configured = host || username || password;
            if (configured && !(host && username && password && from)) {
                throw new IllegalStateException("Local SMTP requires SMTP_HOST, SMTP_USERNAME, SMTP_PASSWORD, and MAIL_FROM; remove all SMTP settings to use the local mail sink");
            }
        };
    }

    @Bean
    @Conditional(MissingSmtpConfiguration.class)
    @Primary
    public JavaMailSender localMailSink() {
        return new JavaMailSenderImpl() {
            @Override
            public void send(SimpleMailMessage message) {
                log.info("LOCAL MAIL SINK (not delivered)\nTo: {}\nSubject: {}\n{}",
                        String.join(", ", message.getTo()), message.getSubject(), message.getText());
            }
        };
    }

    private static boolean hasValue(Environment environment, String property) {
        return !environment.getProperty(property, "").isBlank();
    }

    public static class MissingSmtpConfiguration implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String host = context.getEnvironment().getProperty("spring.mail.host", "");
            String username = context.getEnvironment().getProperty("spring.mail.username", "");
            String password = context.getEnvironment().getProperty("spring.mail.password", "");
            return host.isBlank() && username.isBlank() && password.isBlank();
        }
    }
}
