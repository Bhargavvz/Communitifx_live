package com.cofix.cofixBackend.Configurations;

import com.cofix.cofixBackend.Services.AuthService;
import com.cofix.cofixBackend.Services.CofixService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {
//
//    @Bean
//    @Order(1)
//    public AuthService authService() {
//        return new AuthService();
//    }
//
//    @Bean
//    @Order(2)
//    public CofixService cofixService() {
//        return new CofixService();
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}