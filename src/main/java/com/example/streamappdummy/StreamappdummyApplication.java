package com.example.streamappdummy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan( basePackages = {"com.uhx.platform.streamingapp", "com.example.streamappdummy"})
public class StreamappdummyApplication {

	public static void main(String[] args) {
		SpringApplication.run(StreamappdummyApplication.class, args);
	}

}
