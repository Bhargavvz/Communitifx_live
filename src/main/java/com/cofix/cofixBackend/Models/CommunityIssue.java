package com.cofix.cofixBackend.Models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "community_issues", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityIssue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String description;
    private String category;
    private String urgency;
    private String status;
    private Double latitude;
    private Double longitude;
    
    @Column(name = "photo_url")
    private String photoUrl;
    
    @Column(name = "user_email")
    private String userEmail;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_forum")
    private Boolean isForum;

    @Column(name = "upvotes")
    private Integer upvotes;

    @Column(name = "downvotes")
    private Integer downvotes;

    @Column(name = "is_volunteer")
    private Boolean isVolunteer;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "spots_available")
    private Integer spotsAvailable;

    @Column(name = "max_spots")
    private Integer maxSpots;

    @Column(name = "required_skills")
    private String requiredSkills;
} 