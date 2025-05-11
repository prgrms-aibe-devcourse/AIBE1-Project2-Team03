package aibe.hosik.review;

import aibe.hosik.apply.Apply;
import aibe.hosik.apply.repository.ApplyRepository;
import aibe.hosik.common.dto.ApiResponse;
import aibe.hosik.common.exception.ResourceNotFoundException;
import aibe.hosik.common.exception.AccessDeniedException;
import aibe.hosik.profile.Profile;
import aibe.hosik.profile.ProfileService;
import aibe.hosik.user.User;
import aibe.hosik.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
  private final ReviewService reviewService;
  private final UserRepository userRepository;
  private final ApplyRepository applyRepository;
  private final ProfileService profileService;

  /**
   * 프로필 리뷰 작성
   */
  @PostMapping("/profile/{userId}")
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
  @PostMapping("/peer/{applyId}")
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
  @GetMapping("/peer/{applyId}")
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
   * 특정 사용자의 프로필 리뷰 목록 조회
   */
  @GetMapping("/profile/{userId}")
  public ResponseEntity<?> getProfileReviews(@PathVariable Long userId) {
    try {
      List<Review> profileReviews = reviewService.getProfileReviews(userId);
      log.info("Retrieved {} profile reviews for user {}", profileReviews.size(), userId);
      return ResponseEntity.ok(profileReviews);
    } catch (Exception e) {
      log.error("Error retrieving profile reviews for user {}: {}", userId, e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  /**
   * 사용자의 모든 리뷰 조회 (프로필 리뷰 + 피어리뷰)
   */
  @GetMapping("/user/{userId}/all")
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
  @DeleteMapping("/{reviewId}")
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
}