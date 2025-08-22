package com.dipal.NovaCare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
public class NovaCareApplication {

	public static void main(String[] args) {
		SpringApplication.run(NovaCareApplication.class, args);
	}

}
