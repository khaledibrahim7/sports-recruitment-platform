package com.sports.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.sports"})
@EntityScan(basePackages = {"com.sports"})
@EnableJpaRepositories(basePackages = {"com.sports"})
@EnableCaching
public class SportsRecruitmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(SportsRecruitmentApplication.class, args);
    }
}
