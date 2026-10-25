package com.sports.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/** Local-only mail sink. Never activate the local profile in production. */
@Configuration
@Profile("local")
public class LocalMailConfiguration {
    private static final Logger log = LoggerFactory.getLogger(LocalMailConfiguration.class);

    @Bean
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
}
