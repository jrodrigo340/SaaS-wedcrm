package com.wedcrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WedcrmApplication {

	public static void main(String[] args) {
		SpringApplication.run(WedcrmApplication.class, args);
	}

}
