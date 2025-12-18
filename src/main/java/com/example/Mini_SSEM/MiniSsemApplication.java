package com.example.Mini_SSEM;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MiniSsemApplication {

	public static void main(String[] args) {
		SpringApplication.run(MiniSsemApplication.class, args);
	}

}
