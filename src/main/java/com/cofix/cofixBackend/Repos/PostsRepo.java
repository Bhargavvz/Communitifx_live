package com.cofix.cofixBackend.Repos;

import com.cofix.cofixBackend.Models.BenefitTypes;
import com.cofix.cofixBackend.Models.MyPost;
import com.cofix.cofixBackend.Models.PostPk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostsRepo extends JpaRepository<MyPost, Long> {
    List<MyPost> findByEmail(String email);

    List<MyPost> findByEmailAndBenefitType(String email, BenefitTypes benefitType);

    Optional<MyPost> findByPostId(Long postId);

    List<MyPost> findByBenefitType(BenefitTypes benefitType);

    @Query("SELECT p FROM MyPost p WHERE p.isForum = true")
    List<MyPost> findByIsForumTrue();

    @Query("SELECT p FROM MyPost p WHERE p.isVolunteer = true")
    List<MyPost> findByIsVolunteerTrue();

    // Add query for community issues only
    @Query("SELECT p FROM MyPost p WHERE p.benefitType = com.cofix.cofixBackend.Models.BenefitTypes.COMMUNITY_ISSUE AND p.isForum = false AND p.isVolunteer = false")
    List<MyPost> findCommunityIssues();

    void deleteByPostId(Long postId);
}