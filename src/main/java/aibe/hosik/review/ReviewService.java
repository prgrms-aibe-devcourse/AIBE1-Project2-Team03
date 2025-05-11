package aibe.hosik.review;

import aibe.hosik.apply.entity.Apply;
import aibe.hosik.apply.repository.ApplyRepository;
import aibe.hosik.common.exception.AccessDeniedException;
import aibe.hosik.common.exception.ResourceNotFoundException;
import aibe.hosik.user.User;
import aibe.hosik.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final UserRepository userRepository;
  private final ApplyRepository applyRepository;

  /**
   * 프로필 리뷰 작성
   */
  @Transactional
  public Review addProfileReview(Long reviewerId, Long revieweeId, String content) {
    log.info("Adding profile review from user {} to user {}", reviewerId, revieweeId);

    User reviewer = userRepository.findById(reviewerId)
            .orElseThrow(() -> new ResourceNotFoundException("리뷰어를 찾을 수 없습니다: " + reviewerId));

    User reviewee = userRepository.findById(revieweeId)
            .orElseThrow(() -> new ResourceNotFoundException("리뷰이를 찾을 수 없습니다: " + revieweeId));

    // 자기 자신에게 리뷰 작성 방지
    if (reviewerId.equals(revieweeId)) {
      throw new IllegalArgumentException("자기 자신에게 리뷰를 작성할 수 없습니다.");
    }

    Review review = Review.createProfileReview(reviewer, reviewee, content);
    return reviewRepository.save(review);
  }

  /**
   * 피어리뷰 작성
   */
  @Transactional
  public Review addPeerReview(Long reviewerId, Long revieweeId, Long applyId, String content, Integer rating) {
    log.info("Adding peer review from user {} to user {} for apply {}", reviewerId, revieweeId, applyId);

    User reviewer = userRepository.findById(reviewerId)
            .orElseThrow(() -> new ResourceNotFoundException("리뷰어를 찾을 수 없습니다: " + reviewerId));

    User reviewee = userRepository.findById(revieweeId)
            .orElseThrow(() -> new ResourceNotFoundException("리뷰이를 찾을 수 없습니다: " + revieweeId));

    Apply apply = applyRepository.findById(applyId)
            .orElseThrow(() -> new ResourceNotFoundException("프로젝트를 찾을 수 없습니다: " + applyId));

    // 자기 자신에게 리뷰 작성 방지
    if (reviewerId.equals(revieweeId)) {
      throw new IllegalArgumentException("자기 자신에게 리뷰를 작성할 수 없습니다.");
    }

    // 프로젝트 권한 확인 (리뷰어, 리뷰이 모두 프로젝트 참여자여야 함)
    if (!(apply.getUser().getId().equals(reviewerId) || apply.getPost().getUser().getId().equals(reviewerId)) ||
            !(apply.getUser().getId().equals(revieweeId) || apply.getPost().getUser().getId().equals(revieweeId))) {
      throw new AccessDeniedException("프로젝트 참여자만 피어리뷰를 작성할 수 있습니다.");
    }

    // 이미 작성한 리뷰가 있는지 확인
    if (reviewRepository.findPeerReviewByReviewerAndRevieweeAndApply(reviewerId, revieweeId, applyId).isPresent()) {
      throw new IllegalStateException("이미 이 프로젝트에서 해당 사용자에게 리뷰를 작성했습니다.");
    }

    Review review = Review.createPeerReview(reviewer, reviewee, apply, content, rating);
    return reviewRepository.save(review);
  }

  /**
   * 리뷰 삭제
   */
  @Transactional
  public void deleteReview(Long reviewId, Long userId) {
    log.info("Deleting review ID: {} by user ID: {}", reviewId, userId);

    Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("리뷰를 찾을 수 없습니다: " + reviewId));

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));

    // 삭제 권한 확인
    if (!review.canBeDeletedBy(user)) {
      throw new AccessDeniedException("리뷰를 삭제할 권한이 없습니다.");
    }

    reviewRepository.delete(review);
    log.info("Review deleted. ID: {}", reviewId);
  }

  /**
   * 프로필 리뷰 목록 조회
   */
  @Transactional(readOnly = true)
  public List<Review> getProfileReviews(Long userId) {
    log.info("Getting profile reviews for user ID: {}", userId);
    return reviewRepository.findProfileReviewsByRevieweeId(userId);
  }

  /**
   * 특정 프로젝트의 피어리뷰 목록 조회
   */
  @Transactional(readOnly = true)
  public List<Review> getPeerReviews(Long applyId, Long userId) {
    log.info("Getting peer reviews for apply ID: {} by user ID: {}", applyId, userId);

    Apply apply = applyRepository.findById(applyId)
            .orElseThrow(() -> new ResourceNotFoundException("프로젝트를 찾을 수 없습니다: " + applyId));

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));

    // 접근 권한 확인 (프로젝트 참여자만)
    if (!(apply.getUser().getId().equals(userId) || apply.getPost().getUser().getId().equals(userId))) {
      throw new AccessDeniedException("프로젝트 참여자만 피어리뷰를 조회할 수 있습니다.");
    }

    return reviewRepository.findPeerReviewsByApplyId(applyId);
  }

  /**
   * 사용자가 받은 모든 리뷰 조회 (프로필 리뷰 + 피어리뷰)
   */
  @Transactional(readOnly = true)
  public List<Review> getAllReceivedReviews(Long userId) {
    log.info("Getting all received reviews for user ID: {}", userId);
    return reviewRepository.findAllByRevieweeId(userId);
  }

  /**
   * 사용자가 작성한 모든 리뷰 조회 (프로필 리뷰 + 피어리뷰)
   */
  @Transactional(readOnly = true)
  public List<Review> getAllWrittenReviews(Long userId) {
    log.info("Getting all written reviews for user ID: {}", userId);
    return reviewRepository.findAllByReviewerId(userId);
  }
}