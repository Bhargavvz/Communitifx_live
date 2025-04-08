package com.cofix.cofixBackend.Services;

import com.cofix.cofixBackend.Models.MyUser;
import com.cofix.cofixBackend.Repos.UsersRepo;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@Order(10) // Lower priority than DatabaseInitializer
@DependsOn("databaseInitializer") // Explicitly depend on DatabaseInitializer
public class AuthService implements Ordered {

    @Autowired
    private UsersRepo userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${admin-email}")
    String adminEmail;

    @PostConstruct
    public void initCofix(){
        try {
            // Add admin data
            MyUser adminUser = null;
            try {
                adminUser = userRepository.findByEmail(adminEmail);
            } catch (DataAccessException e) {
                log.error("Error checking for admin user: {}", e.getMessage());
                log.warn("Database tables may not be initialized yet. Check DatabaseInitializer logs.");
                return;
            }
            
            if(adminUser == null) {
                log.info("++++++++++++++ CREATING ADMIN USER ++++++++++++++");
                userRepository.save(new MyUser("admin", adminEmail, passwordEncoder.encode("admin")));
            }
            
            // Add test user data
            MyUser testUser = null;
            try {
                testUser = userRepository.findByEmail("test@user.com");
            } catch (DataAccessException e) {
                log.error("Error checking for test user: {}", e.getMessage());
                return;
            }
            
            if(testUser == null) {
                log.info("++++++++++++++ CREATING TEST USER ++++++++++++++++");
                userRepository.save(new MyUser("test@user.com", "Test User", passwordEncoder.encode("password"), "testy", "1234567890", "India", "Male", "Telangana", LocalDateTime.now()));
            }
            log.info("======================= AuthService initialized =======================");
        } catch (Exception e) {
            log.error("Error during AuthService initialization: {}", e.getMessage());
            log.warn("Application will continue, but authentication functionality may be limited");
        }
    }

    public boolean loginUser(String email, String rawPassword) {
        try {
            MyUser user = userRepository.findByEmail(email);
            if (user != null) {
                return passwordEncoder.matches(rawPassword, user.getPassword());
            }
            return false;
        } catch (DataAccessException e) {
            log.error("Database error during login attempt: {}", e.getMessage());
            return false;
        }
    }

    public MyUser registerUser(MyUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreateDate(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
