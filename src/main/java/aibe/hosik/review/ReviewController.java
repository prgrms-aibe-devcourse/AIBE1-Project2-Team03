package aibe.hosik.review;

import aibe.hosik.review.dto.ReviewRequest;
import aibe.hosik.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Post", description = "모집글 API") // Swagger Tag
public class ReviewController {
  private final ReviewService reviewService;

  @SecurityRequirement(name = "JWT")
  @Operation(summary = "리뷰 등록")
  @PostMapping("{revieweeId}")
  @ResponseStatus(HttpStatus.CREATED)
  public void createReview(
      @PathVariable("revieweeId") Long revieweeId,
      @RequestBody ReviewRequest request,
      @AuthenticationPrincipal User user
  ) {
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }

    reviewService.createReview(request, revieweeId, user);
  }

  @SecurityRequirement(name = "JWT")
  @Operation(summary = "리뷰 수정")
  @PatchMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateReview(
      @PathVariable("id") Long reviewId,
      @RequestBody ReviewRequest request,
      @AuthenticationPrincipal User user
  ) {
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }

    reviewService.updateReview(request, reviewId, user);
  }

  @SecurityRequirement(name = "JWT")
  @Operation(summary = "리뷰 삭제")
  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteReview(
      @PathVariable("id") Long reviewId,
      @AuthenticationPrincipal User user
  ) {
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }

    reviewService.deleteReview(reviewId, user);
  }
}
