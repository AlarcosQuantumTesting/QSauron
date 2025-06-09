package edu.uclm.reper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Spring Boot Application class
 */
@SpringBootApplication
@EntityScan("edu.uclm.reper.model")
@EnableJpaRepositories("edu.uclm.reper.repository")
public class ReperApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReperApplication.class, args);
    }
}
