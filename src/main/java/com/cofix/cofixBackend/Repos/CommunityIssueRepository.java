package com.cofix.cofixBackend.Repos;

import com.cofix.cofixBackend.Models.CommunityIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityIssueRepository extends JpaRepository<CommunityIssue, Long> {
    List<CommunityIssue> findByIsForumTrue();
    List<CommunityIssue> findByIsVolunteerTrue();
} 