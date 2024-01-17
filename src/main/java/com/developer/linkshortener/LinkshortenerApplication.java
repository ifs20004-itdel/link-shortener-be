package com.developer.linkshortener;

import com.developer.linkshortener.controller.UrlResourceController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class LinkshortenerApplication {
	public static void main(String[] args) {
		SpringApplication.run(LinkshortenerApplication.class, args);
	}
}
