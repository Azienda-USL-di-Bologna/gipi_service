package it.bologna.ausl.gipi;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.sql2o.Sql2o;

@SpringBootApplication(scanBasePackages = {"it.nextsw", "it.bologna.ausl"})
public class SpringlingoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringlingoApplication.class, args);
    }

}
