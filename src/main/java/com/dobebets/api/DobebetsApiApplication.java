package com.dobebets.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling 
@SpringBootApplication
public class DobebetsApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(DobebetsApiApplication.class, args);
	}

}
