package it.bologna.ausl.gipi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableLoadTimeWeaving
@EnableCaching
//@EnableScheduling
@SpringBootApplication(scanBasePackages = {"it.nextsw", "it.bologna.ausl"})
public class SpringlingoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringlingoApplication.class, args);
    }
}
