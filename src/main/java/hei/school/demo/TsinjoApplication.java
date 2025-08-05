package hei.school.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TsinjoApplication {
    public static void main(String[] args) {
        SpringApplication.run(TsinjoApplication.class, args);
    }
}
