package aibe.hosik.profile;

import aibe.hosik.apply.entity.Apply;
import aibe.hosik.common.dto.ApiResponse;
import aibe.hosik.common.exception.ResourceNotFoundException;
import aibe.hosik.post.entity.Post;
import aibe.hosik.profile.dto.ProfileDTO;
import aibe.hosik.profile.dto.ProfileUpdateRequest;
import aibe.hosik.profile.dto.ProfileVisibilityRequest;
import aibe.hosik.resume.dto.ResumeDTO;
import aibe.hosik.resume.entity.Resume;
import aibe.hosik.review.Review;
import aibe.hosik.review.ReviewService;
import aibe.hosik.review.dto.ReviewRequest;
import aibe.hosik.review.dto.ReviewDTO;
import aibe.hosik.user.User;
import aibe.hosik.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "프로필 관리 API")
@SecurityRequirement(name = "JWT")
public class ProfileController {
    private final ProfileService profileService;
    private final ReviewService reviewService;
    private final UserRepository userRepository;

    /**
     * 현재 로그인한 사용자의 프로필 조회 (마이페이지)
     */
    @Operation(summary = "내 프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyProfile(@AuthenticationPrincipal User user) {
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
        response.put("profile", ProfileDTO.from(profile));
        response.put("recentResumes", recentResumes.stream().map(ResumeDTO::from).collect(Collectors.toList()));
        response.put("appliedPosts", appliedPosts.getContent());
        response.put("createdPosts", createdPosts.getContent());
        response.put("isOwner", true);

        log.info("Retrieved profile for user: {}", user.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 사용자의 프로필 조회
     */
    @Operation(summary = "특정 사용자 프로필 조회", description = "특정 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "프로필을 찾을 수 없음")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<?> getProfileByUserId(
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        try {
            // 대표 자기소개서를 포함한 프로필 조회
            Profile profile = profileService.getProfileWithMainResume(userId);

            boolean isOwner = currentUser.getId().equals(userId);

            // 프로필 공개 여부 확인
            if (!profile.isPublic() && !isOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(false, "비공개 프로필입니다."));
            }

            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("profile", ProfileDTO.from(profile));
            response.put("isOwner", isOwner);

            // 리뷰 목록도 함께 조회
            List<ReviewDTO> reviewDTOs = profile.getReceivedReviews().stream()
                    .map(ReviewDTO::from)
                    .collect(Collectors.toList());
            response.put("reviews", reviewDTOs);

            // 프로필 DTO 생성시 리뷰를 포함하는 경우
            ProfileDTO profileWithReviews = ProfileDTO.withReviews(profile, reviewDTOs);

            // 본인인 경우 추가 데이터 제공
            if (isOwner) {
                // 최근 자기소개서 5개 조회
                List<Resume> recentResumes = profileService.getRecentResumes(userId, 5);
                response.put("recentResumes", recentResumes.stream()
                        .map(ResumeDTO::from)
                        .collect(Collectors.toList()));

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
     * 프로필 정보 업데이트
     */
    @Operation(summary = "프로필 정보 업데이트", description = "사용자의 프로필 정보를 업데이트합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "업데이트 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(
            @RequestBody ProfileUpdateRequest request,
            @AuthenticationPrincipal User user) {

        try {
            Profile updatedProfile = profileService.updateProfile(
                    user.getId(),
                    request.introduction(),
                    request.image()
            );

            log.info("Updated profile for user: {}", user.getEmail());
            return ResponseEntity.ok(ProfileDTO.from(updatedProfile));
        } catch (Exception e) {
            log.error("Error updating profile for user {}: {}", user.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 프로필 공개 여부 설정
     */
    @Operation(summary = "프로필 공개 여부 설정", description = "프로필의 공개 여부를 설정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "설정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PutMapping("/visibility")
    public ResponseEntity<?> updateProfileVisibility(
            @RequestBody ProfileVisibilityRequest request,
            @AuthenticationPrincipal User user) {

        try {
            Profile updatedProfile = profileService.updateProfileVisibility(user.getId(), request.isPublic());
            log.info("Updated profile visibility for user: {}, isPublic: {}", user.getEmail(), request.isPublic());
            return ResponseEntity.ok(ProfileDTO.from(updatedProfile));
        } catch (Exception e) {
            log.error("Error updating profile visibility for user {}: {}", user.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 프로필 이미지 업로드
     */
    @Operation(summary = "프로필 이미지 업로드", description = "사용자의 프로필 이미지를 업로드합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "업로드 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProfileImage(
            @RequestParam("image") MultipartFile imageFile,
            @AuthenticationPrincipal User user) {

        try {
            Profile updatedProfile = profileService.updateProfileImage(user.getId(), imageFile);
            log.info("Updated profile image for user: {}", user.getEmail());
            return ResponseEntity.ok(Map.of("image", updatedProfile.getImage()));
        } catch (Exception e) {
            log.error("Error uploading profile image for user {}: {}", user.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 리뷰 작성
     */
    @Operation(summary = "리뷰 작성", description = "특정 사용자에게 리뷰를 작성합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "작성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/{userId}/reviews")
    public ResponseEntity<?> addReview(
            @PathVariable Long userId,
            @RequestBody ReviewRequest request,
            @AuthenticationPrincipal User currentUser) {

        // 자기 자신에게 리뷰 작성 방지
        if (currentUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "자기 자신에게 리뷰를 작성할 수 없습니다."));
        }

        try {
            Review review = reviewService.addReview(currentUser.getId(), userId, request.content());

            log.info("Added review from user {} to user {}", currentUser.getId(), userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(ReviewDTO.from(review));
        } catch (Exception e) {
            log.error("Error adding review from user {} to user {}: {}", currentUser.getId(), userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 리뷰 삭제
     */
    @Operation(summary = "리뷰 삭제", description = "작성된 리뷰를 삭제합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal User currentUser) {

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
    @Operation(summary = "자기소개서 관리 페이지 조회", description = "자기소개서 관리 페이지 데이터를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/resumes")
    public ResponseEntity<Page<ResumeDTO>> getResumeManagement(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Resume> resumes = profileService.getRecentResumes(user.getId(), pageable);
        Page<ResumeDTO> resumeDTOs = resumes.map(ResumeDTO::from);

        log.info("Retrieved resume management data for user: {}", user.getEmail());
        return ResponseEntity.ok(resumeDTOs);
    }

    /**
     * 지원한 모집글 목록
     */
    @Operation(summary = "지원한 모집글 목록 조회", description = "사용자가 지원한 모집글 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/applies")
    public ResponseEntity<Page<Apply>> getAppliedPosts(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Apply> applies = profileService.getAppliedPosts(user.getId(), pageable);

        log.info("Retrieved applied posts for user: {}", user.getEmail());
        return ResponseEntity.ok(applies);
    }

    /**
     * 작성한 모집글 목록
     */
    @Operation(summary = "작성한 모집글 목록 조회", description = "사용자가 작성한 모집글 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/posts")
    public ResponseEntity<Page<Post>> getCreatedPosts(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = profileService.getCreatedPosts(user.getId(), pageable);

        log.info("Retrieved created posts for user: {}", user.getEmail());
        return ResponseEntity.ok(posts);
    }

    /**
     * 공개 프로필 목록 조회
     */
    @Operation(summary = "공개 프로필 목록 조회", description = "공개 설정된 모든 프로필 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/public")
    public ResponseEntity<List<ProfileDTO>> getPublicProfiles() {
        List<Profile> publicProfiles = profileService.getAllPublicProfiles();
        List<ProfileDTO> profileDTOs = publicProfiles.stream()
                .map(ProfileDTO::summary)
                .collect(Collectors.toList());

        log.info("Retrieved {} public profiles", publicProfiles.size());
        return ResponseEntity.ok(profileDTOs);
    }

    /**
     * 공개 프로필 검색
     */
    @Operation(summary = "공개 프로필 검색", description = "키워드로 공개 프로필을 검색합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "검색 성공")
    })
    @GetMapping("/search")
    public ResponseEntity<List<ProfileDTO>> searchProfiles(
            @Parameter(description = "검색 키워드") @RequestParam String keyword) {

        List<Profile> profiles = profileService.searchPublicProfiles(keyword);
        List<ProfileDTO> profileDTOs = profiles.stream()
                .map(ProfileDTO::summary)
                .collect(Collectors.toList());

        log.info("Found {} profiles with keyword: {}", profiles.size(), keyword);
        return ResponseEntity.ok(profileDTOs);
    }

    /**
     * 특정 사용자의 포트폴리오 조회
     */
    @Operation(summary = "특정 사용자의 포트폴리오 조회", description = "특정 사용자의 포트폴리오를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음")
    })
    @GetMapping("/{userId}/portfolios")
    public ResponseEntity<?> getUserPortfolios(
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {

        try {
            List<Resume> portfolios = profileService.getPortfolios(userId, currentUser);
            List<ResumeDTO> portfolioDTOs = portfolios.stream()
                    .map(ResumeDTO::from)
                    .collect(Collectors.toList());

            log.info("Retrieved {} portfolios for user ID: {}", portfolios.size(), userId);
            return ResponseEntity.ok(portfolioDTOs);
        } catch (Exception e) {
            log.error("Error retrieving portfolios for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}