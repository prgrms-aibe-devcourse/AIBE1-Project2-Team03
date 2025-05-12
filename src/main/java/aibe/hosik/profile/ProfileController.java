package aibe.hosik.profile;

import aibe.hosik.post.dto.PostResponseDTO;
import aibe.hosik.post.service.PostService;
import aibe.hosik.profile.dto.ProfileDetailResponse;
import aibe.hosik.profile.dto.ProfileResponse;
import aibe.hosik.resume.dto.ResumeResponse;
import aibe.hosik.resume.service.ResumeService;
import aibe.hosik.review.ReviewService;
import aibe.hosik.review.dto.ReviewResponse;
import aibe.hosik.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "프로필 API") // Swagger Tag
public class ProfileController {
  private final ProfileService profileService;
  private final PostService postService;
  private final ReviewService reviewService;
  private final ResumeService resumeService;

  /**
   * 현재 로그인한 사용자의 프로필 조회 (마이페이지)
   */
  @SecurityRequirement(name = "JWT")
  @Operation(summary = "마이프로필 조회")
  @GetMapping("me")
  public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal User user) {
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }

    Long userId = user.getId();

    ProfileResponse profile = profileService.getProfileByUserId(userId);
    List<PostResponseDTO> authorPosts = postService.getAllPostsCreatedByAuthor(userId);
    List<PostResponseDTO> joinedPosts = postService.getAllPostsJoinedByUser(userId);
    List<ReviewResponse> reviews = reviewService.getAllReviewsByUserId(userId);
    List<ResumeResponse> resumes = resumeService.getAllResumesByUserId(userId);

    ProfileDetailResponse response = ProfileDetailResponse.from(profile, authorPosts, joinedPosts, reviews, resumes);

    return ResponseEntity.ok(response);
  }
//
//    /**
//     * 프로필 정보 업데이트 (모달에서 사용)
//     */
//    @PutMapping("/update")
//    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> profileData) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String email = authentication.getName();
//
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));
//
//        try {
//            String introduction = profileData.get("introduction");
//            String imageUrl = profileData.get("image");
//
//            Profile updatedProfile = profileService.updateProfile(user.getId(), introduction, imageUrl);
//            log.info("Updated profile for user: {}", email);
//            return ResponseEntity.ok(updatedProfile);
//        } catch (Exception e) {
//            log.error("Error updating profile for user {}: {}", email, e.getMessage());
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(new ApiResponse(false, e.getMessage()));
//        }
//    }
//
//    /**
//     * 자기소개서 관리 페이지 데이터
//     */
//    @GetMapping("/resumes")
//    public ResponseEntity<?> getResumeManagement(@RequestParam(defaultValue = "0") int page,
//                                                 @RequestParam(defaultValue = "10") int size) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String email = authentication.getName();
//
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));
//
//        Pageable pageable = PageRequest.of(page, size);
//        Page<Resume> resumes = profileService.getRecentResumes(user.getId(), pageable);
//
//        log.info("Retrieved resume management data for user: {}", email);
//        return ResponseEntity.ok(resumes);
//    }
//
//    /**
//     * 특정 사용자의 포트폴리오 조회
//     */
//    @GetMapping("/{userId}/portfolios")
//    public ResponseEntity<?> getUserPortfolios(@PathVariable Long userId) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String email = authentication.getName();
//
//        User currentUser = userRepository.findByEmail(email)
//                .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));
//
//        try {
//            List<Resume> portfolios = profileService.getPortfolios(userId, currentUser);
//            log.info("Retrieved {} portfolios for user ID: {}", portfolios.size(), userId);
//            return ResponseEntity.ok(portfolios);
//        } catch (Exception e) {
//            log.error("Error retrieving portfolios for user ID {}: {}", userId, e.getMessage());
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(new ApiResponse(false, e.getMessage()));
//        }
//    }
}