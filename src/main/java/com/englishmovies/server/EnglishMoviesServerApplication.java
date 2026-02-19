package com.englishmovies.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EnglishMoviesServerApplication {

	public static void main(String[] args) {
		System.setProperty("liquibase.secureParsing", "false");
		SpringApplication.run(EnglishMoviesServerApplication.class, args);
	}

}
