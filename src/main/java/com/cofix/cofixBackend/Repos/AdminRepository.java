package com.cofix.cofixBackend.Repos;

import com.cofix.cofixBackend.Models.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<AdminUser, String> {
} 