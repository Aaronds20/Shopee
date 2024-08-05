package com.shopee.shopee;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.shopee.shopee.daos.UserRepository;
import com.shopee.shopee.entities.User;



@SpringBootApplication
public class ShopeeApplication implements CommandLineRunner{
@Autowired
	public BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepo;

	public static void main(String[] args) {
		SpringApplication.run(ShopeeApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		User user = new User();
			
		user.setId(1);
		user.setEmail("aa@gmail.com");
		user.setEnable(true);
		user.setName("Aaron Dsouza");
		user.setPhone("1234567890");
		user.setRole("ROLE_ADMIN");
		user.setPassword(passwordEncoder.encode("admin"));
		user.setProfile("admin.png");
		user.setDate(new Date());
		
		this.userRepo.save(user);
		
		
		
	}

}

