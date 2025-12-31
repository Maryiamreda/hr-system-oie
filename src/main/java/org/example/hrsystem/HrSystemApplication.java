package org.example.hrsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
@SpringBootApplication
@EnableJpaRepositories
public class HrSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(HrSystemApplication.class, args);
        System.out.println("السلام عليكم");
    }
}
