package aibe.hosik.profile;

import aibe.hosik.apply.Apply;
import aibe.hosik.apply.service.ApplyService;
import aibe.hosik.apply.repository.ApplyRepository;
import aibe.hosik.post.Post;
import aibe.hosik.resume.Resume;
import aibe.hosik.review.Review;
import aibe.hosik.review.ReviewService;
import aibe.hosik.user.User;
import aibe.hosik.user.UserRepository;
import aibe.hosik.common.dto.ApiResponse;
import aibe.hosik.common.exception.ResourceNotFoundException;
import aibe.hosik.common.exception.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final ApplyService applyService;
    private final ApplyRepository applyRepository;

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

            // 프로필 리뷰 목록 조회
            response.put("profileReviews", reviewService.getProfileReviews(userId));

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
     * 프로필 이미지 업로드 (모달에서 사용)
     */
    @PostMapping("/image")
    public ResponseEntity<?> uploadProfileImage(@RequestParam("image") MultipartFile imageFile) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

        try {
            Profile updatedProfile = profileService.updateProfileImage(user.getId(), imageFile);
            log.info("Updated profile image for user: {}", email);
            return ResponseEntity.ok(Map.of("image", updatedProfile.getImage()));
        } catch (IOException e) {
            log.error("Error uploading profile image for user {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 프로필 리뷰 작성
     */
    @PostMapping("/{userId}/reviews")
    public ResponseEntity<?> addProfileReview(@PathVariable Long userId, @RequestBody Map<String, String> reviewData) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

        try {
            String content = reviewData.get("content");
            Review review = reviewService.addProfileReview(currentUser.getId(), userId, content);

            log.info("Added profile review from user {} to user {}", currentUser.getId(), userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(review);
        } catch (Exception e) {
            log.error("Error adding profile review from user {} to user {}: {}", currentUser.getId(), userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 피어리뷰 작성
     */
    @PostMapping("/applies/{applyId}/peer-reviews")
    public ResponseEntity<?> addPeerReview(@PathVariable Long applyId, @RequestBody Map<String, Object> reviewData) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

        try {
            Long revieweeId = Long.parseLong(reviewData.get("revieweeId").toString());
            String content = reviewData.get("content").toString();
            Integer rating = reviewData.get("rating") != null ?
                    Integer.parseInt(reviewData.get("rating").toString()) : null;

            Review review = reviewService.addPeerReview(currentUser.getId(), revieweeId, applyId, content, rating);

            log.info("Added peer review from user {} to user {} for apply {}",
                    currentUser.getId(), revieweeId, applyId);
            return ResponseEntity.status(HttpStatus.CREATED).body(review);
        } catch (Exception e) {
            log.error("Error adding peer review for apply {}: {}", applyId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 특정 프로젝트의 피어리뷰 목록 조회
     */
    @GetMapping("/applies/{applyId}/peer-reviews")
    public ResponseEntity<?> getPeerReviews(@PathVariable Long applyId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

        try {
            List<Review> peerReviews = reviewService.getPeerReviews(applyId, currentUser.getId());
            log.info("Retrieved {} peer reviews for apply {}", peerReviews.size(), applyId);
            return ResponseEntity.ok(peerReviews);
        } catch (Exception e) {
            log.error("Error retrieving peer reviews for apply {}: {}", applyId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 사용자의 모든 리뷰 조회 (프로필 리뷰 + 피어리뷰)
     */
    @GetMapping("/{userId}/reviews/all")
    public ResponseEntity<?> getAllReviews(@PathVariable Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

        // 본인 또는 공개 프로필인 경우만 접근 가능
        Profile profile = profileService.getProfileByUserId(userId);
        if (!profile.canBeViewedBy(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "접근 권한이 없습니다."));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("receivedReviews", reviewService.getAllReceivedReviews(userId));

        // 본인인 경우 작성한 리뷰도 조회
        if (currentUser.getId().equals(userId)) {
            response.put("writtenReviews", reviewService.getAllWrittenReviews(userId));
        }

        log.info("Retrieved all reviews for user {}", userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 리뷰 삭제 (프로필 리뷰 및 피어리뷰)
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

        try {
            reviewService.deleteReview(reviewId, currentUser.getId());
            log.info("Deleted review ID: {} by user ID: {}", reviewId, currentUser.getId());
            return ResponseEntity.ok(new ApiResponse(true, "리뷰가 삭제되었습니다."));
        } catch (Exception e) {
            log.error("Error deleting review ID: {} by user ID: {}: {}", reviewId, currentUser.getId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 프로젝트 대시보드 (프로젝트에서 팀원들에게 피어리뷰 작성)
     */
    @GetMapping("/projects/{applyId}/dashboard")
    public ResponseEntity<?> getProjectDashboard(@PathVariable Long applyId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

        try {
            Apply apply = applyRepository.findById(applyId)
                    .orElseThrow(() -> new ResourceNotFoundException("프로젝트를 찾을 수 없습니다: " + applyId));

            // 프로젝트 참여자 확인
            if (!apply.getUser().getId().equals(currentUser.getId()) &&
                    !apply.getPost().getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("프로젝트 참여자만 접근할 수 있습니다.");
            }

            // 프로젝트 완료 확인
            if (!apply.isSelected()) {
                throw new IllegalStateException("매칭 성공한 프로젝트만 대시보드에 접근할 수 있습니다.");
            }

            Map<String, Object> response = new HashMap<>();

            // 프로젝트 정보
            response.put("project", apply);
            response.put("post", apply.getPost());

            // 팀원 정보
            List<User> teamMembers = List.of(apply.getUser(), apply.getPost().getUser());
            response.put("teamMembers", teamMembers);

            // 기존 피어리뷰 목록
            response.put("peerReviews", reviewService.getPeerReviews(applyId, currentUser.getId()));

            // 리뷰 작성 가능한 팀원 목록 (자신 제외)
            List<User> reviewableMembers = teamMembers.stream()
                    .filter(member -> !member.getId().equals(currentUser.getId()))
                    .toList();
            response.put("reviewableMembers", reviewableMembers);

            log.info("Retrieved project dashboard for apply {} by user {}", applyId, currentUser.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving project dashboard for apply {}: {}", applyId, e.getMessage());
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
        Page<Resume> resumes = profileService.getResumesWithPaging(user.getId(), pageable);

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
