package aibe.hosik.profile;

import aibe.hosik.apply.Apply;
import aibe.hosik.common.dto.ApiResponse;
import aibe.hosik.common.exception.ResourceNotFoundException;
import aibe.hosik.post.Post;
import aibe.hosik.resume.Resume;
import aibe.hosik.review.Review;
import aibe.hosik.review.ReviewService;
import aibe.hosik.user.User;
import aibe.hosik.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;
    private final ReviewService reviewService;
    private final UserRepository userRepository;

    /**
     * 현재 로그인한 사용자의 프로필 조회 (마이페이지)
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

        // 대표 자기소개서를 포함한 프로필 조회
        Profile profile = profileService.getProfileWithMainResume(user.getId());

        // 최근 자기소개서 5개 조회
        List<Resume> recentResumes = profileService.getRecentResumes(user.getId(), 5);

        // 지원한 모집글 5개 조회
        Pageable pageable = PageRequest.of(0, 5);
        Page<Apply> appliedPosts = profileService.getAppliedPosts(user.getId(), pageable);

        // 작성한 모집글 5개 조회
        Page<Post> createdPosts = profileService.getCreatedPosts(user.getId(), pageable);

        // 응답 데이터 생성
        Map<String, Object> response = new HashMap<>();
        response.put("profile", profile);
        response.put("mainResume", profile.getMainResume());
        response.put("recentResumes", recentResumes);
        response.put("appliedPosts", appliedPosts.getContent());
        response.put("createdPosts", createdPosts.getContent());
        response.put("isOwner", true);

        log.info("Retrieved profile for user: {}", email);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 사용자의 프로필 조회
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getProfileByUserId(@PathVariable Long userId) {
        try {
            // 대표 자기소개서를 포함한 프로필 조회
            Profile profile = profileService.getProfileWithMainResume(userId);

            // 현재 로그인한 사용자 확인 (본인 여부 확인)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User currentUser = userRepository.findByEmail(email).orElse(null);

            boolean isOwner = (currentUser != null && currentUser.getId().equals(userId));

            // 프로필 공개 여부 확인
            if (!profile.isPublic() && !isOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(false, "비공개 프로필입니다."));
            }

            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("profile", profile);
            response.put("mainResume", profile.getMainResume());
            response.put("user", profile.getUser());
            response.put("isOwner", isOwner);

            // 리뷰 목록도 함께 조회
            response.put("reviews", profile.getReceivedReviews());

            // 본인인 경우 추가 데이터 제공
            if (isOwner) {
                // 최근 자기소개서 5개 조회
                List<Resume> recentResumes = profileService.getRecentResumes(userId, 5);
                response.put("recentResumes", recentResumes);

                // 지원한 모집글 5개 조회
                Pageable pageable = PageRequest.of(0, 5);
                Page<Apply> appliedPosts = profileService.getAppliedPosts(userId, pageable);
                response.put("appliedPosts", appliedPosts.getContent());

                // 작성한 모집글 5개 조회
                Page<Post> createdPosts = profileService.getCreatedPosts(userId, pageable);
                response.put("createdPosts", createdPosts.getContent());
            }

            log.info("Retrieved profile for user ID: {}", userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving profile for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 프로필 정보 업데이트 (모달에서 사용)
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> profileData) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

        try {
            String introduction = profileData.get("introduction");
            String imageUrl = profileData.get("image");

            Profile updatedProfile = profileService.updateProfile(user.getId(), introduction, imageUrl);
            log.info("Updated profile for user: {}", email);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            log.error("Error updating profile for user {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 프로필 공개 여부 설정
     */
    @PutMapping("/visibility")
    public ResponseEntity<?> updateProfileVisibility(@RequestBody Map<String, Boolean> visibilityData) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

        try {
            Boolean isPublic = visibilityData.get("isPublic");
            if (isPublic == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(false, "공개 여부 값이 필요합니다."));
            }

            Profile updatedProfile = profileService.updateProfileVisibility(user.getId(), isPublic);
            log.info("Updated profile visibility for user: {}, isPublic: {}", email, isPublic);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            log.error("Error updating profile visibility for user {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 프로필 이미지 업로드 (Supabase 버전)
     */
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProfileImage(@RequestParam("image") MultipartFile imageFile) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

        try {
            Profile updatedProfile = profileService.updateProfileImage(user.getId(), imageFile);
            log.info("Updated profile image for user: {}", email);
            return ResponseEntity.ok(Map.of("image", updatedProfile.getImage()));
        } catch (Exception e) {
            log.error("Error uploading profile image for user {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 리뷰 작성 (모달에서 사용)
     */
    @PostMapping("/{userId}/reviews")
    public ResponseEntity<?> addReview(@PathVariable Long userId, @RequestBody Map<String, String> reviewData) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

        // 자기 자신에게 리뷰 작성 방지
        if (currentUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "자기 자신에게 리뷰를 작성할 수 없습니다."));
        }

        try {
            String content = reviewData.get("content");
            Review review = reviewService.addReview(currentUser.getId(), userId, content);

            log.info("Added review from user {} to user {}", currentUser.getId(), userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(review);
        } catch (Exception e) {
            log.error("Error adding review from user {} to user {}: {}", currentUser.getId(), userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 리뷰 삭제
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

        try {
            profileService.deleteReview(reviewId, currentUser.getId());
            log.info("Deleted review ID: {} by user ID: {}", reviewId, currentUser.getId());
            return ResponseEntity.ok(new ApiResponse(true, "리뷰가 삭제되었습니다."));
        } catch (Exception e) {
            log.error("Error deleting review ID: {} by user ID: {}: {}", reviewId, currentUser.getId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 자기소개서 관리 페이지 데이터
     */
    @GetMapping("/resumes")
    public ResponseEntity<?> getResumeManagement(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

        Pageable pageable = PageRequest.of(page, size);
        Page<Resume> resumes = profileService.getRecentResumes(user.getId(), pageable);

        log.info("Retrieved resume management data for user: {}", email);
        return ResponseEntity.ok(resumes);
    }

    /**
     * 지원한 모집글 목록
     */
    @GetMapping("/applies")
    public ResponseEntity<?> getAppliedPosts(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

        Pageable pageable = PageRequest.of(page, size);
        Page<Apply> applies = profileService.getAppliedPosts(user.getId(), pageable);

        log.info("Retrieved applied posts for user: {}", email);
        return ResponseEntity.ok(applies);
    }

    /**
     * 작성한 모집글 목록
     */
    @GetMapping("/posts")
    public ResponseEntity<?> getCreatedPosts(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = profileService.getCreatedPosts(user.getId(), pageable);

        log.info("Retrieved created posts for user: {}", email);
        return ResponseEntity.ok(posts);
    }

    /**
     * 공개 프로필 목록 조회
     */
    @GetMapping("/public")
    public ResponseEntity<?> getPublicProfiles() {
        List<Profile> publicProfiles = profileService.getAllPublicProfiles();
        log.info("Retrieved {} public profiles", publicProfiles.size());
        return ResponseEntity.ok(publicProfiles);
    }

    /**
     * 공개 프로필 검색
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchProfiles(@RequestParam String keyword) {
        List<Profile> profiles = profileService.searchPublicProfiles(keyword);
        log.info("Found {} profiles with keyword: {}", profiles.size(), keyword);
        return ResponseEntity.ok(profiles);
    }

    /**
     * 특정 사용자의 포트폴리오 조회
     */
    @GetMapping("/{userId}/portfolios")
    public ResponseEntity<?> getUserPortfolios(@PathVariable Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

        try {
            List<Resume> portfolios = profileService.getPortfolios(userId, currentUser);
            log.info("Retrieved {} portfolios for user ID: {}", portfolios.size(), userId);
            return ResponseEntity.ok(portfolios);
        } catch (Exception e) {
            log.error("Error retrieving portfolios for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}