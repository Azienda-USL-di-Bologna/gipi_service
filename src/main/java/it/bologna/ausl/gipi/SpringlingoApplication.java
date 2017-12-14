package it.bologna.ausl.gipi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//
@SpringBootApplication(scanBasePackages = {"it.nextsw", "it.bologna.ausl"})
public class SpringlingoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringlingoApplication.class, args);
    }
}
