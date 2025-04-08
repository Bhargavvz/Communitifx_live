package com.cofix.cofixBackend.Controllers;

import com.cofix.cofixBackend.Models.BenefitTypes;
import com.cofix.cofixBackend.Models.MyPost;
import com.cofix.cofixBackend.Models.MyReview;
import com.cofix.cofixBackend.Models.MyUser;
import com.cofix.cofixBackend.Models.CommunityIssue;
import com.cofix.cofixBackend.Models.Location;
import com.cofix.cofixBackend.Models.AdminUser;
import com.cofix.cofixBackend.Models.PostPk;
import com.cofix.cofixBackend.Services.AuthService;
import com.cofix.cofixBackend.Services.CofixService;
import com.cofix.cofixBackend.Services.EmailSenderService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.Base64;
import java.util.stream.Collectors;
import java.util.TreeMap;

@RestController
@Slf4j
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", methods = {
        RequestMethod.GET,
        RequestMethod.POST,
        RequestMethod.PUT,
        RequestMethod.DELETE,
        RequestMethod.OPTIONS
})
@RequestMapping("/api")
public class CofixLoginController {

    private static final Logger logger = LoggerFactory.getLogger(CofixLoginController.class);

    @Autowired
    AuthService authService;

    @Autowired
    CofixService cofixService;

    @Value("${admin-email}")
    String adminEmail;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public CofixLoginController() {
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestParam String email, @RequestParam String password) {
        logger.info("Login attempt for email: " + email);

        // Add input validation
        if (email == null || password == null ||
                email.trim().isEmpty() || password.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("message", "Email and password are required"));
        }

        if (authService.loginUser(email, password)) {
            logger.info("User authenticated successfully: " + email);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("email", email);
            return ResponseEntity.ok(response);
        } else {
            logger.info("Authentication failed for: " + email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("message", "Invalid email or password"));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signUp(@RequestParam String name, @RequestParam String email,
            @RequestParam String password) {
        logger.info("Signup information has been received successfully:");

        // Add input validation
        if (name == null || email == null || password == null ||
                name.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "All fields are required"));
        }

        Optional<MyUser> user = cofixService.getUsersRepo().findById(email);
        if (user.isPresent()) {
            logger.error("Cannot create user, email already exists: " + email);
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Email already registered"));
        } else {
            MyUser newUser = new MyUser(name, email, password);
            newUser.setCreateDate(LocalDateTime.now());
            authService.registerUser(newUser);
            logger.info("New User added: " + email);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Sign-up successful");
            response.put("email", email);
            return ResponseEntity.ok(response);
        }
    }

    @Autowired
    private EmailSenderService emailSenderService;

    @GetMapping("/hello")
    public void sendMail() throws MessagingException {
        MyPost post = new MyPost(
                "test@user.com", // email
                BenefitTypes.GOVERNMENT_SCHEME, // benefitType
                "Rythu Bandhu", // schemeName
                "Rythu Bandhu description", // description
                null, // image
                "Rythu Bandhu", // issueName
                null, // activityDescription
                new Location(17.455598622434977, 78.66648576707394), // location
                "Rythu Bandhu Description" // comment
        );

        cofixService.sendNotificationEmail(post, adminEmail);
    }

    @GetMapping("/profile")
    public ResponseEntity<MyUser> getProfileData(@RequestParam String email) {
        logger.info("Profile API: Fetching profile information for email: " + email);
        try {
            Optional<MyUser> user = cofixService.getUsersRepo().findById(email);
            if (user.isPresent()) {
                logger.info("User found: " + user.get());
                return ResponseEntity.ok(user.get());
            } else {
                logger.info("User not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error fetching profile: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // @PostMapping("/profile/edit")
    // public ResponseEntity<MyUser> setProfileData(String email) {
    // logger.info("Profile API: Sending profile information with email:" + email);
    // Optional<MyUser> user = cofixService.getUsersRepo().findById(email);
    // if(user.isPresent()){
    // logger.info("User found :" + user);
    // logger.info("Updating info : " + user.get());
    // return ResponseEntity.ok(user.get());
    // } else{
    // logger.info("User not found");1eb6
    // return ResponseEntity.notFound().build();
    // }
    // }

    @PostMapping("/profile/update")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<MyUser> updateProfile(@RequestBody MyUser updatedProfile) {
        logger.info("Updating profile for user: " + updatedProfile.getEmail());
        try {
            Optional<MyUser> existingProfile = cofixService.getUsersRepo().findById(updatedProfile.getEmail());
            if (existingProfile.isPresent()) {
                MyUser currentProfile = existingProfile.get();

                // Preserve the password and creation date
                updatedProfile.setPassword(currentProfile.getPassword());
                updatedProfile.setCreateDate(currentProfile.getCreateDate());

                // Update other fields
                MyUser savedProfile = cofixService.getUsersRepo().save(updatedProfile);
                logger.info("Profile updated successfully: " + savedProfile);
                return ResponseEntity.ok(savedProfile);
            } else {
                logger.error("Profile not found for email: " + updatedProfile.getEmail());
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error updating profile: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @CrossOrigin
    @GetMapping("/profile/posts")
    public ResponseEntity<List<MyPost>> showAllPosts(String email) {
        List<MyPost> posts = cofixService.getProfilePosts(email);
        if (!posts.isEmpty()) {
            logger.info("Get All posts for user: " + posts);
            return new ResponseEntity<>(posts, HttpStatus.OK);
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/issues")
    public ResponseEntity<List<Map<String, Object>>> getAllCommunityIssues(String benefitType) {
        try {
            List<MyPost> allPosts;
            if (benefitType != null) {
                // If benefitType is specified, get specific type
                allPosts = cofixService.getPostsRepo().findByBenefitType(BenefitTypes.valueOf(benefitType));
            } else {
                // If no benefitType specified, get all posts
                allPosts = cofixService.getPostsRepo().findAll();
            }

            if (!allPosts.isEmpty()) {
                List<Map<String, Object>> enrichedIssues = allPosts.stream().map(issue -> {
                    Map<String, Object> enrichedIssue = new HashMap<>();
                    enrichedIssue.put("id", issue.getPostId());
                    enrichedIssue.put("title", issue.getIssueName());
                    enrichedIssue.put("description", issue.getDescription());
                    enrichedIssue.put("location", issue.getLocation());
                    enrichedIssue.put("image", issue.getImage());
                    enrichedIssue.put("status", issue.getStatus());
                    enrichedIssue.put("urgency", issue.getUrgency());
                    enrichedIssue.put("userEmail", issue.getEmail());
                    enrichedIssue.put("createdAt", issue.getFormattedDate());
                    enrichedIssue.put("category", issue.getComment());
                    enrichedIssue.put("benefitType", issue.getBenefitType());
                    enrichedIssue.put("schemeName", issue.getSchemeName());
                    return enrichedIssue;
                }).collect(Collectors.toList());

                return new ResponseEntity<>(enrichedIssues, HttpStatus.OK);
            }
            return ResponseEntity.ok(new ArrayList<>());
        } catch (Exception e) {
            logger.error("Error fetching issues: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // @CrossOrigin
    @GetMapping("/profile/issues/all")
    public ResponseEntity<List<MyPost>> showAllIssues(String email) {
        List<MyPost> allPosts = new ArrayList<>();

        try {
            // Get community issues
            List<MyPost> issues = cofixService.getProfileIssues(email);
            if (issues != null) {
                issues.forEach(issue -> issue.setBenefitType(BenefitTypes.COMMUNITY_ISSUE));
                allPosts.addAll(issues);
            }

            // Get government schemes
            List<MyPost> schemes = cofixService.getProfileSchemes(email);
            if (schemes != null) {
                schemes.forEach(scheme -> scheme.setBenefitType(BenefitTypes.GOVERNMENT_SCHEME));
                allPosts.addAll(schemes);
            }

            return ResponseEntity.ok(allPosts);
        } catch (Exception e) {
            logger.error("Error fetching issues: ", e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @GetMapping("/profile/issues/community")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<List<MyPost>> showAllCommunityIssues(@RequestParam String email) {
        try {
            List<MyPost> issues = cofixService.getPostsRepo()
                    .findByEmailAndBenefitType(email, BenefitTypes.COMMUNITY_ISSUE);

            // Additional filter to ensure only community issues
            issues = issues.stream()
                    .filter(post -> BenefitTypes.COMMUNITY_ISSUE.equals(post.getBenefitType()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(issues);
        } catch (Exception e) {
            logger.error("Error fetching community issues: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/profile/schemes")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<List<MyPost>> showAllSchemes(@RequestParam String email) {
        try {
            List<MyPost> schemes = cofixService.getPostsRepo()
                    .findByEmailAndBenefitType(email, BenefitTypes.GOVERNMENT_SCHEME);
            return ResponseEntity.ok(schemes);
        } catch (Exception e) {
            logger.error("Error fetching schemes: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @CrossOrigin
    @PostMapping("/profile/issues/add")
    public ResponseEntity<MyPost> addIssue(@RequestBody MyPost issuePost) {
        try {
            // Set default values if not provided
            if (issuePost.getUrgency() == null) {
                issuePost.setUrgency("MEDIUM");
            }
            if (issuePost.getStatus() == null) {
                issuePost.setStatus("PENDING");
            }
            if (issuePost.getBenefitType() == null) {
                issuePost.setBenefitType(BenefitTypes.COMMUNITY_ISSUE);
            }

            MyPost savedPost = cofixService.addIssuePost(issuePost);
            if (savedPost != null) {
                try {
                    cofixService.sendNotificationEmail(savedPost, savedPost.getEmail());
                } catch (MessagingException e) {
                    logger.error("Failed to send notification email", e);
                }
                return ResponseEntity.ok(savedPost);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            logger.error("Error adding issue: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/profile/issues/{postId}")
    public ResponseEntity<?> deleteIssue(@PathVariable("postId") Long postId) {
        try {
            // First find the post
            Optional<MyPost> post = cofixService.getPostsRepo().findByPostId(postId);
            if (post.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Create PostPk with both email and postId
            MyPost postToDelete = post.get();
            PostPk postPk = new PostPk(postToDelete.getEmail(), postId);

            // Delete using the composite key
            cofixService.getPostsRepo().deleteById(postPk);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting issue: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting issue: " + e.getMessage());
        }
    }

    @CrossOrigin
    @PostMapping("/profile/schemes/add")
    public ResponseEntity<MyPost> addScheme(@RequestBody MyPost schemePost) {

        logger.info("Scheme to be added :" + schemePost);
        MyPost addedPost = cofixService.addSchemePost(schemePost);
        if (addedPost != null) {
            logger.info("Successfully added scheme post");
        } else {
            logger.error("Failed to add scheme post");
        }
        logger.info("New community post added for user: " + addedPost);
        return new ResponseEntity<>(addedPost, HttpStatus.CREATED);
    }

    @CrossOrigin
    @PostMapping("/profile/review/add")
    public ResponseEntity<MyReview> addIssue(@RequestBody MyReview review) {
        // Save the issue to the database or in-memory store
        // For now, just print it to the console
        logger.info("review to be added :" + review);

        MyReview finalReview = cofixService.addReview(review);
        if (finalReview != null) {
            logger.info("Successfully added review");
        } else {
            logger.error("Failed to add issue post");
        }
        logger.info("New community post added for user: " + finalReview);
        return new ResponseEntity<>(finalReview, HttpStatus.CREATED);
    }

    @PostMapping("/issues/report")
    public ResponseEntity<?> reportIssue(@RequestParam String title,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam String urgency,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam String userEmail,
            @RequestParam(required = false) MultipartFile image) {
        try {
            // Validate input
            if (title == null || description == null || userEmail == null ||
                    title.trim().isEmpty() || description.trim().isEmpty() || userEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Collections.singletonMap("message", "Required fields are missing"));
            }

            String base64Image = null;
            if (image != null && !image.isEmpty()) {
                base64Image = saveImage(image); // This will now store as Base64
            }

            MyPost post = new MyPost(
                    userEmail,
                    BenefitTypes.COMMUNITY_ISSUE,
                    title,
                    description,
                    base64Image, // Store Base64 string
                    title,
                    null,
                    new Location(latitude, longitude),
                    category);
            post.setUrgency(urgency);
            post.setStatus("PENDING");

            MyPost savedPost = cofixService.addIssuePost(post);
            return ResponseEntity.ok(savedPost);
        } catch (Exception e) {
            logger.error("Error reporting issue:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Failed to report issue: " + e.getMessage()));
        }
    }

    private String saveImage(MultipartFile file) {
        try {
            // Convert image to Base64 string
            byte[] imageBytes = file.getBytes();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            logger.error("Error converting image to Base64: ", e);
            throw new RuntimeException("Failed to process image", e);
        }
    }

    @GetMapping("/issues/all")
    public ResponseEntity<List<MyPost>> getAllIssues() {
        try {
            List<MyPost> allPosts = cofixService.getPostsRepo().findAll();

            if (allPosts != null) {
                allPosts.forEach(post -> {
                    // Set default benefit type if not set
                    if (post.getBenefitType() == null) {
                        post.setBenefitType(BenefitTypes.COMMUNITY_ISSUE);
                    }

                    // Set default location if not set
                    if (post.getLatitude() == null || post.getLongitude() == null) {
                        post.setLatitude(17.455598622434977);
                        post.setLongitude(78.66648576707394);
                    }

                    // Set the location object
                    post.setLocation(new Location(post.getLatitude(), post.getLongitude()));
                });

                logger.info("Found {} total issues", allPosts.size());
                return ResponseEntity.ok(allPosts);
            }

            return ResponseEntity.ok(new ArrayList<>());
        } catch (Exception e) {
            logger.error("Error fetching all issues: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    // Add this method to handle location data
    private Location getLocationFromPost(MyPost post) {
        if (post.getLocation() != null) {
            return post.getLocation();
        }
        return new Location(17.455598622434977, 78.66648576707394);
    }

    @GetMapping("/auth/status")
    public ResponseEntity<Map<String, Object>> checkAuthStatus(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        Optional<MyUser> user = cofixService.getUsersRepo().findById(email);

        if (user.isPresent()) {
            response.put("isAuthenticated", true);
            response.put("user", user.get());
            return ResponseEntity.ok(response);
        }

        response.put("isAuthenticated", false);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestParam String email) {
        // Clear any server-side session if needed
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/login")
    public ResponseEntity<Map<String, String>> adminLogin(
            @RequestParam String email,
            @RequestParam String password) {
        logger.info("Admin login attempt for email: " + email);

        if (email == null || password == null ||
                email.trim().isEmpty() || password.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("message", "Email and password are required"));
        }

        Optional<AdminUser> adminOpt = cofixService.getAdminByEmail(email);

        if (adminOpt.isPresent() && passwordEncoder.matches(password, adminOpt.get().getPassword())) {
            AdminUser admin = adminOpt.get();
            admin.setLastLogin(LocalDateTime.now());
            cofixService.saveAdmin(admin);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("email", email);
            response.put("adminLevel", admin.getAdminLevel().toString());
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Collections.singletonMap("message", "Invalid credentials"));
    }

    @PostMapping("/admin/signup")
    public ResponseEntity<Map<String, String>> adminSignup(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam Integer adminLevel,
            @RequestParam String adminCode) {
        logger.info("Admin signup attempt for email: " + email);

        // Validate input
        if (name == null || email == null || password == null || adminCode == null ||
                name.trim().isEmpty() || email.trim().isEmpty() ||
                password.trim().isEmpty() || adminCode.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("message", "All fields are required"));
        }

        // Validate admin level
        if (adminLevel < 1 || adminLevel > 4) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("message", "Invalid admin level"));
        }

        // Validate admin code format
        if (!adminCode.matches("^[0-9]{6}$")) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("message", "Invalid admin code format"));
        }

        Optional<AdminUser> existingAdmin = cofixService.getAdminByEmail(email);
        if (existingAdmin.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("message", "Email already registered"));
        }

        AdminUser admin = new AdminUser();
        admin.setName(name);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setAdminLevel(adminLevel);
        admin.setAdminCode(adminCode);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setIssuesResolved(0);

        cofixService.saveAdmin(admin);

        return ResponseEntity.ok(Collections.singletonMap("message", "Admin registered successfully"));
    }

    @PostMapping("/posts")
    public ResponseEntity<MyPost> createPost(@RequestParam String email,
            @RequestParam BenefitTypes benefitType,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam(required = false) String image,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam String comment) {
        try {
            MyPost post = new MyPost(
                    email,
                    benefitType,
                    title, // schemeName
                    description,
                    image,
                    title, // issueName
                    null, // activityDescription
                    new Location(latitude, longitude),
                    comment);

            MyPost savedPost = cofixService.addIssuePost(post);
            return ResponseEntity.ok(savedPost);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/profile/issues")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<List<MyPost>> getIssues(@RequestParam String email,
            @RequestParam(required = false) String type) {
        try {
            List<MyPost> posts;
            if (type != null && type.equals("COMMUNITY_ISSUE")) {
                posts = cofixService.getPostsRepo().findByEmailAndBenefitType(email, BenefitTypes.COMMUNITY_ISSUE);
            } else {
                posts = cofixService.getPostsRepo().findByEmail(email);
            }
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            logger.error("Error fetching issues: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Add this endpoint for updating issues
    @PutMapping("/profile/issues/update/{postId}")
    public ResponseEntity<MyPost> updateIssue(@PathVariable Long postId, @RequestBody MyPost updatedPost) {
        try {
            // Find the existing post
            Optional<MyPost> existingPost = cofixService.getPostsRepo()
                    .findById(new PostPk(updatedPost.getEmail(), postId));

            if (existingPost.isPresent()) {
                MyPost post = existingPost.get();

                // Update the fields
                post.setDescription(updatedPost.getDescription());
                post.setIssueName(updatedPost.getIssueName());
                post.setSchemeName(updatedPost.getSchemeName());
                post.setComment(updatedPost.getComment());
                post.setUrgency(updatedPost.getUrgency());
                post.setStatus(updatedPost.getStatus());

                if (updatedPost.getLocation() != null) {
                    post.setLocation(updatedPost.getLocation());
                }

                if (updatedPost.getImage() != null) {
                    post.setImage(updatedPost.getImage());
                }

                // Save the updated post
                MyPost savedPost = cofixService.getPostsRepo().save(post);
                return ResponseEntity.ok(savedPost);
            }

            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating issue: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/admin/issues")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<List<Map<String, Object>>> getAllIssuesForAdmin() {
        try {
            List<MyPost> allPosts = cofixService.getPostsRepo().findAll();

            if (!allPosts.isEmpty()) {
                List<Map<String, Object>> enrichedIssues = allPosts.stream().map(issue -> {
                    Map<String, Object> enrichedIssue = new HashMap<>();
                    enrichedIssue.put("id", issue.getPostId());
                    enrichedIssue.put("title", issue.getIssueName());
                    enrichedIssue.put("description", issue.getDescription());
                    enrichedIssue.put("location", issue.getLocation());
                    enrichedIssue.put("image", issue.getImage());
                    enrichedIssue.put("status", issue.getStatus());
                    enrichedIssue.put("urgency", issue.getUrgency());
                    enrichedIssue.put("userEmail", issue.getEmail());
                    enrichedIssue.put("createdAt", issue.getFormattedDate());
                    enrichedIssue.put("category", issue.getComment());
                    enrichedIssue.put("benefitType", issue.getBenefitType());
                    enrichedIssue.put("schemeName", issue.getSchemeName());
                    return enrichedIssue;
                }).collect(Collectors.toList());

                return ResponseEntity.ok(enrichedIssues);
            }
            return ResponseEntity.ok(new ArrayList<>());
        } catch (Exception e) {
            logger.error("Error fetching admin issues: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/admin/profile/update")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<AdminUser> updateAdminProfile(@RequestBody AdminUser updatedAdmin) {
        try {
            Optional<AdminUser> existingAdmin = cofixService.getAdminByEmail(updatedAdmin.getEmail());
            if (existingAdmin.isPresent()) {
                AdminUser currentAdmin = existingAdmin.get();

                // Preserve sensitive fields
                updatedAdmin.setPassword(currentAdmin.getPassword());
                updatedAdmin.setCreatedAt(currentAdmin.getCreatedAt());
                updatedAdmin.setAdminCode(currentAdmin.getAdminCode());
                updatedAdmin.setIssuesResolved(currentAdmin.getIssuesResolved());

                AdminUser savedAdmin = cofixService.saveAdmin(updatedAdmin);
                return ResponseEntity.ok(savedAdmin);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating admin profile: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/admin/issues/update/{postId}")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<MyPost> updateIssueStatus(
            @PathVariable Long postId,
            @RequestParam String status,
            @RequestParam String adminEmail) {
        try {
            Optional<MyPost> postOpt = cofixService.getPostsRepo().findByPostId(postId);
            if (postOpt.isPresent()) {
                MyPost post = postOpt.get();
                post.setStatus(status);

                // Update admin's issues resolved count if marking as solved
                if (status.equalsIgnoreCase("solved")) {
                    cofixService.incrementAdminIssuesResolved(adminEmail);
                }

                MyPost updatedPost = cofixService.getPostsRepo().save(post);
                return ResponseEntity.ok(updatedPost);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating issue status: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/admin/dashboard-stats")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<Map<String, Object>> getDashboardStats(@RequestParam String adminEmail) {
        try {
            Map<String, Object> stats = new HashMap<>();

            // Get all issues
            List<MyPost> allIssues = cofixService.getAllIssues();

            // Calculate monthly stats with proper sorting
            Map<String, Integer> monthlyStats = new TreeMap<>(); // Using TreeMap for natural ordering
            String[] months = { "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC" };

            // Initialize all months with 0
            for (String month : months) {
                monthlyStats.put(month, 0);
            }

            // Update counts for months that have issues
            for (MyPost issue : allIssues) {
                String month = issue.getCreateDate().getMonth().toString().substring(0, 3).toUpperCase();
                monthlyStats.merge(month, 1, Integer::sum);
            }

            // Convert to list format for frontend
            List<Map<String, Object>> issuesByMonth = monthlyStats.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> monthData = new HashMap<>();
                        monthData.put("month", entry.getKey());
                        monthData.put("count", entry.getValue());
                        return monthData;
                    })
                    .collect(Collectors.toList());

            // Calculate various issue counts
            long totalIssues = allIssues.size();
            long pendingIssues = allIssues.stream()
                    .filter(issue -> "PENDING".equalsIgnoreCase(issue.getStatus()))
                    .count();
            long resolvedIssues = allIssues.stream()
                    .filter(issue -> "SOLVED".equalsIgnoreCase(issue.getStatus()))
                    .count();
            long communityIssues = allIssues.stream()
                    .filter(issue -> BenefitTypes.COMMUNITY_ISSUE.equals(issue.getBenefitType()))
                    .count();
            long governmentSchemes = allIssues.stream()
                    .filter(issue -> BenefitTypes.GOVERNMENT_SCHEME.equals(issue.getBenefitType()))
                    .count();
            long criticalIssues = allIssues.stream()
                    .filter(issue -> "HIGH".equalsIgnoreCase(issue.getUrgency()))
                    .count();

            // Get admin's resolved issues
            Optional<AdminUser> adminOpt = cofixService.getAdminByEmail(adminEmail);
            int adminResolvedIssues = adminOpt.map(AdminUser::getIssuesResolved).orElse(0);

            // Get recent activity (last 5 issues) with proper sorting
            List<Map<String, Object>> recentActivity = allIssues.stream()
                    .sorted((a, b) -> b.getCreateDate().compareTo(a.getCreateDate()))
                    .limit(5)
                    .map(issue -> {
                        Map<String, Object> activity = new HashMap<>();
                        activity.put("id", issue.getPostId());
                        activity.put("title", issue.getIssueName());
                        activity.put("type", issue.getBenefitType());
                        activity.put("status", issue.getStatus());
                        activity.put("date", issue.getFormattedDate());
                        activity.put("userEmail", issue.getEmail());
                        activity.put("urgency", issue.getUrgency());
                        activity.put("description", issue.getDescription());
                        return activity;
                    })
                    .collect(Collectors.toList());

            // Compile all stats
            stats.put("totalIssues", totalIssues);
            stats.put("pendingIssues", pendingIssues);
            stats.put("resolvedIssues", resolvedIssues);
            stats.put("communityIssues", communityIssues);
            stats.put("governmentSchemes", governmentSchemes);
            stats.put("criticalIssues", criticalIssues);
            stats.put("adminResolvedIssues", adminResolvedIssues);
            stats.put("issuesByMonth", issuesByMonth);
            stats.put("recentActivity", recentActivity);

            // Calculate resolution rate
            double resolutionRate = totalIssues > 0 ? ((double) resolvedIssues / totalIssues) * 100 : 0;
            stats.put("resolutionRate", Math.round(resolutionRate * 100.0) / 100.0);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error fetching dashboard stats: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/admin/profile")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<AdminUser> getAdminProfile(@RequestParam String email) {
        try {
            Optional<AdminUser> admin = cofixService.getAdminByEmail(email);
            if (admin.isPresent()) {
                return ResponseEntity.ok(admin.get());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching admin profile: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/api/admin/issues/{postId}/resolve")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<?> resolveIssue(@PathVariable("postId") Long postId,
            @RequestBody Map<String, Object> resolveData) {
        try {
            Optional<MyPost> post = cofixService.getPostsRepo().findByPostId(postId);
            if (post.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            MyPost issueToResolve = post.get();
            issueToResolve.setStatus("RESOLVED");

            String resolutionDescription = (String) resolveData.get("resolutionDescription");
            issueToResolve.setResolutionDescription(resolutionDescription);

            // Handle base64 image if present
            String base64Image = (String) resolveData.get("resolutionImage");
            if (base64Image != null && !base64Image.isEmpty()) {
                issueToResolve.setResolutionImage(base64Image);
            }

            MyPost savedPost = cofixService.getPostsRepo().save(issueToResolve);

            Map<String, Object> response = new HashMap<>();
            response.put("id", savedPost.getPostId());
            response.put("status", savedPost.getStatus());
            response.put("resolutionDescription", savedPost.getResolutionDescription());
            response.put("image", savedPost.getImage());
            response.put("resolutionImage", savedPost.getResolutionImage());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error resolving issue: " + e.getMessage());
        }
    }

    // Forum endpoints
    @GetMapping("/forums")
    public ResponseEntity<List<MyPost>> getForums() {
        try {
            List<MyPost> forums = cofixService.getPostsRepo().findByIsForumTrue();
            return ResponseEntity.ok(forums);
        } catch (Exception e) {
            logger.error("Error fetching forums: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/forums/create")
    public ResponseEntity<?> createForum(@RequestBody Map<String, String> forumData) {
        try {
            String userEmail = forumData.get("authorEmail");
            if (userEmail == null) {
                return ResponseEntity.badRequest().build();
            }

            MyPost forum = new MyPost();
            forum.setEmail(userEmail);
            forum.setBenefitType(BenefitTypes.FORUM);
            forum.setIsForum(true);
            forum.setIsVolunteer(false);
            forum.setIssueName(forumData.get("title"));
            forum.setDescription(forumData.get("description"));
            forum.setComment(forumData.get("category"));
            forum.setCreateDate(LocalDateTime.now());
            forum.setStatus("ACTIVE");
            forum.setUpvotes(0);
            forum.setDownvotes(0);

            MyPost savedForum = cofixService.getPostsRepo().save(forum);
            return ResponseEntity.ok(savedForum);
        } catch (Exception e) {
            logger.error("Error creating forum: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/forums/{postId}/vote")
    public ResponseEntity<?> voteForum(@PathVariable Long postId, @RequestBody Map<String, String> voteData) {
        try {
            Optional<MyPost> forumOpt = cofixService.getPostsRepo().findByPostId(postId);
            if (forumOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            MyPost forum = forumOpt.get();
            String voteType = voteData.get("type");

            if ("up".equals(voteType)) {
                forum.setUpvotes(forum.getUpvotes() != null ? forum.getUpvotes() + 1 : 1);
            } else if ("down".equals(voteType)) {
                forum.setDownvotes(forum.getDownvotes() != null ? forum.getDownvotes() + 1 : 1);
            }

            cofixService.getPostsRepo().save(forum);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error voting on forum: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // Volunteer endpoints
    @GetMapping("/volunteer/opportunities")
    public ResponseEntity<List<MyPost>> getVolunteerOpportunities() {
        try {
            List<MyPost> opportunities = cofixService.getPostsRepo().findByIsVolunteerTrue();
            return ResponseEntity.ok(opportunities);
        } catch (Exception e) {
            logger.error("Error fetching volunteer opportunities: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/volunteer/opportunities/create")
    public ResponseEntity<?> createVolunteerOpportunity(@RequestBody Map<String, Object> opportunityData) {
        try {
            String userEmail = (String) opportunityData.get("organizerEmail");
            if (userEmail == null) {
                return ResponseEntity.badRequest().build();
            }

            MyPost opportunity = new MyPost();
            opportunity.setEmail(userEmail);
            opportunity.setBenefitType(BenefitTypes.VOLUNTEER_ACTIVITY);
            opportunity.setIsVolunteer(true);
            opportunity.setIsForum(false);
            opportunity.setIssueName((String) opportunityData.get("title"));
            opportunity.setDescription((String) opportunityData.get("description"));
            opportunity.setComment((String) opportunityData.get("category"));
            opportunity.setCreateDate(LocalDateTime.now());
            opportunity.setStatus("ACTIVE");
            opportunity.setEventDate(LocalDate.parse((String) opportunityData.get("eventDate")));
            opportunity.setStartTime(LocalTime.parse((String) opportunityData.get("startTime")));
            opportunity.setEndTime(LocalTime.parse((String) opportunityData.get("endTime")));
            opportunity.setMaxSpots(Integer.parseInt(opportunityData.get("maxSpots").toString()));
            opportunity.setSpotsAvailable(Integer.parseInt(opportunityData.get("maxSpots").toString()));
            opportunity.setRequiredSkills((String) opportunityData.get("requiredSkills"));

            MyPost savedOpportunity = cofixService.getPostsRepo().save(opportunity);
            return ResponseEntity.ok(savedOpportunity);
        } catch (Exception e) {
            logger.error("Error creating volunteer opportunity: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/volunteer/register")
    public ResponseEntity<?> registerForOpportunity(@RequestParam Long opportunityId,
            @RequestParam String volunteerEmail) {
        try {
            Optional<MyPost> opportunityOpt = cofixService.getPostsRepo().findByPostId(opportunityId);
            if (opportunityOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            MyPost opportunity = opportunityOpt.get();
            if (opportunity.getSpotsAvailable() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Collections.singletonMap("error", "No spots available"));
            }

            // Create registration
            MyReview registration = new MyReview();
            registration.setEmail(volunteerEmail);
            registration.setMessage("Volunteer Registration for Post: " + opportunityId);
            registration.setName("Volunteer");
            registration.setCreateDate(LocalDateTime.now());
            cofixService.getReviewsRepo().save(registration);

            // Update spots available
            opportunity.setSpotsAvailable(opportunity.getSpotsAvailable() - 1);
            cofixService.getPostsRepo().save(opportunity);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error registering for opportunity: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/community-issues")
    public ResponseEntity<List<MyPost>> getCommunityIssues() {
        try {
            List<MyPost> issues = cofixService.getPostsRepo().findCommunityIssues();
            return ResponseEntity.ok(issues);
        } catch (Exception e) {
            logger.error("Error fetching community issues: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
