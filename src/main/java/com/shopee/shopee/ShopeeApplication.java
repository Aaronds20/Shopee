package com.shopee.shopee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;



@SpringBootApplication
@ComponentScan("com.shopee.shopee.*")
public class ShopeeApplication {
	public static void main(String[] args) {
		SpringApplication.run(ShopeeApplication.class);
	}

		
	}

