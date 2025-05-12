package aibe.hosik.review;

import aibe.hosik.common.dto.ApiResponse;
import aibe.hosik.review.dto.ReviewDTO;
import aibe.hosik.review.dto.ReviewRequest;
import aibe.hosik.user.User;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "리뷰 관리 API")
@SecurityRequirement(name = "JWT")
public class ReviewController {
  private final ReviewService reviewService;

  /**
   * 내가 작성한 리뷰 목록 조회
   */
  @Operation(summary = "내가 작성한 리뷰 목록 조회", description = "현재 로그인한 사용자가 작성한 리뷰 목록을 조회합니다.")
  @ApiResponses(value = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
  })
  @GetMapping("/written")
  public ResponseEntity<Page<ReviewDTO>> getMyWrittenReviews(
          @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
          @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
          @AuthenticationPrincipal User user) {

    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Review> reviews = reviewService.getReviewsByReviewerId(user.getId(), pageable);
    Page<ReviewDTO> reviewDTOs = reviews.map(ReviewDTO::from);

    log.info("Retrieved {} reviews written by user ID: {}", reviews.getTotalElements(), user.getId());
    return ResponseEntity.ok(reviewDTOs);
  }

  /**
   * 내가 받은 리뷰 목록 조회
   */
  @Operation(summary = "내가 받은 리뷰 목록 조회", description = "현재 로그인한 사용자가 받은 리뷰 목록을 조회합니다.")
  @ApiResponses(value = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
  })
  @GetMapping("/received")
  public ResponseEntity<Page<ReviewDTO>> getMyReceivedReviews(
          @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
          @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
          @AuthenticationPrincipal User user) {

    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Review> reviews = reviewService.getReviewsByRevieweeId(user.getId(), pageable);
    Page<ReviewDTO> reviewDTOs = reviews.map(ReviewDTO::from);

    log.info("Retrieved {} reviews received by user ID: {}", reviews.getTotalElements(), user.getId());
    return ResponseEntity.ok(reviewDTOs);
  }

  /**
   * 특정 사용자에게 리뷰 작성
   */
  @Operation(summary = "리뷰 작성", description = "특정 사용자에게 리뷰를 작성합니다.")
  @ApiResponses(value = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "작성 성공"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
  })
  @PostMapping("/users/{userId}")
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
      log.info("Added review from user ID: {} to user ID: {}", currentUser.getId(), userId);
      return ResponseEntity.status(HttpStatus.CREATED).body(ReviewDTO.from(review));
    } catch (Exception e) {
      log.error("Error adding review from user ID: {} to user ID: {}: {}",
              currentUser.getId(), userId, e.getMessage());
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
  @DeleteMapping("/{reviewId}")
  public ResponseEntity<?> deleteReview(
          @PathVariable Long reviewId,
          @AuthenticationPrincipal User currentUser) {

    try {
      reviewService.deleteReview(reviewId, currentUser.getId());
      log.info("Deleted review ID: {} by user ID: {}", reviewId, currentUser.getId());
      return ResponseEntity.ok(new ApiResponse(true, "리뷰가 삭제되었습니다."));
    } catch (Exception e) {
      log.error("Error deleting review ID: {} by user ID: {}: {}",
              reviewId, currentUser.getId(), e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  /**
   * 특정 사용자가 받은 리뷰 목록 조회
   */
  @Operation(summary = "특정 사용자가 받은 리뷰 목록 조회", description = "특정 사용자가 받은 리뷰 목록을 조회합니다.")
  @ApiResponses(value = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
  })
  @GetMapping("/users/{userId}")
  public ResponseEntity<?> getUserReviews(
          @PathVariable Long userId,
          @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
          @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {

    try {
      Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
      Page<Review> reviews = reviewService.getReviewsByRevieweeId(userId, pageable);
      Page<ReviewDTO> reviewDTOs = reviews.map(ReviewDTO::from);

      log.info("Retrieved {} reviews for user ID: {}", reviews.getTotalElements(), userId);
      return ResponseEntity.ok(reviewDTOs);
    } catch (Exception e) {
      log.error("Error retrieving reviews for user ID: {}: {}", userId, e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(new ApiResponse(false, "해당 사용자의 리뷰를 찾을 수 없습니다."));
    }
  }
}