package com.cofix.cofixBackend.Models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUser {
    @Id
    private String email;
    private String name;
    private String password;
    
    @Column(name = "admin_level")
    private Integer adminLevel;
    
    @Column(name = "admin_code")
    private String adminCode;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(name = "issues_resolved")
    private Integer issuesResolved = 0;
} 