package com.osttra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ManytomanyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ManytomanyApplication.class, args);
		
		System.setProperty("spring.data.mongodb.host", "localhost");
		System.setProperty("spring.data.mongodb.port", "27017");
		System.setProperty("spring.data.mongodb.database", "mongoprac");

	}

}
