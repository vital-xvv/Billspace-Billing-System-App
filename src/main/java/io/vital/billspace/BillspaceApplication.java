package io.vital.billspace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@SpringBootApplication//(exclude = {SecurityAutoConfiguration.class})
public class BillspaceApplication {
	private static final int STRENGTH = 11;

	public static void main(String[] args) {
		SpringApplication.run(BillspaceApplication.class, args);
	}

	@Bean
	public BCryptPasswordEncoder getBCryptPasswordEncoder(){
		return new BCryptPasswordEncoder(STRENGTH);
	}

}
