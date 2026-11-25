package com.shapay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShapayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShapayApplication.class, args);
	}

}
