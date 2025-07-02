package com.fluffb4ll.WebGimmicks;

import com.fluffb4ll.WebGimmicks.repositories.LinkShortenerRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WebGimmicksApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebGimmicksApplication.class, args);
	}
}
