package com.cofix.cofixBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.cofix.cofixBackend.Controllers",
    "com.cofix.cofixBackend.Services",
    "com.cofix.cofixBackend.Configurations"
})
public class CofixBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CofixBackendApplication.class, args);
	}

}
