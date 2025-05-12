package aibe.hosik.review;

import aibe.hosik.common.exception.AccessDeniedException;
import aibe.hosik.common.exception.ResourceNotFoundException;
import aibe.hosik.user.User;
import aibe.hosik.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
  private final ReviewRepository reviewRepository;
  private final UserRepository userRepository;

  /**
   * 리뷰 작성
   */
  @Transactional
  public Review addReview(Long reviewerId, Long revieweeId, String content) {
    log.info("Adding review from user ID: {} to user ID: {}", reviewerId, revieweeId);

    User reviewer = userRepository.findById(reviewerId)
            .orElseThrow(() -> new ResourceNotFoundException("리뷰 작성자를 찾을 수 없습니다."));

    User reviewee = userRepository.findById(revieweeId)
            .orElseThrow(() -> new ResourceNotFoundException("리뷰 대상자를 찾을 수 없습니다."));

    if (reviewerId.equals(revieweeId)) {
      throw new IllegalArgumentException("자기 자신에게 리뷰를 작성할 수 없습니다.");
    }

    Review review = Review.builder()
            .content(content)
            .reviewer(reviewer)
            .reviewee(reviewee)
            .build();

    return reviewRepository.save(review);
  }

  /**
   * 리뷰 삭제
   */
  @Transactional
  public void deleteReview(Long reviewId, Long userId) {
    log.info("Deleting review ID: {} by user ID: {}", reviewId, userId);

    Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("리뷰를 찾을 수 없습니다."));

    // 리뷰 작성자 또는 리뷰 대상자만 삭제 가능
    if (!review.getReviewer().getId().equals(userId) &&
            !review.getReviewee().getId().equals(userId)) {
      throw new AccessDeniedException("이 리뷰를 삭제할 권한이 없습니다.");
    }

    reviewRepository.delete(review);
  }

  /**
   * 사용자가 작성한 리뷰 목록 조회
   */
  @Transactional(readOnly = true)
  public Page<Review> getReviewsByReviewerId(Long reviewerId, Pageable pageable) {
    log.info("Getting reviews written by user ID: {}", reviewerId);
    return reviewRepository.findByReviewerId(reviewerId, pageable);
  }

  /**
   * 사용자가 받은 리뷰 목록 조회
   */
  @Transactional(readOnly = true)
  public Page<Review> getReviewsByRevieweeId(Long revieweeId, Pageable pageable) {
    log.info("Getting reviews received by user ID: {}", revieweeId);
    return reviewRepository.findByRevieweeId(revieweeId, pageable);
  }
}