package it.bologna.ausl.gipi.config.spring;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {
    "it.bologna.ausl.gipi.service",
    "it.bologna.ausl.entities.repository"
})
@EntityScan(basePackages = {
    "it.bologna.ausl.entities",
    "it.bologna.ausl.views"
})

public class SpringWebConfig {
}
