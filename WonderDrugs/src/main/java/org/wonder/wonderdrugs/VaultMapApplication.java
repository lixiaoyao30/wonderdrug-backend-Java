package org.wonder.wonderdrugs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VaultMapApplication {
    public static void main(String[] args) {
        SpringApplication.run(VaultMapApplication.class, args);
    }
}
