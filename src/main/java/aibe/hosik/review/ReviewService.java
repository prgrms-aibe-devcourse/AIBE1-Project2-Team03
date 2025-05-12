package aibe.hosik.review;

import aibe.hosik.handler.exception.CustomException;
import aibe.hosik.handler.exception.ErrorCode;
import aibe.hosik.review.dto.ReviewResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
  private final ReviewRepository reviewRepository;

  public List<ReviewResponse> getAllReviewsByUserId(Long userId) {
    return reviewRepository.findAllByUserId(userId)
        .stream()
        .map(ReviewResponse::from)
        .toList();
  }

  public ReviewResponse getReviewDetail(Long reviewId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_REVIEW));
    return ReviewResponse.from(review);
  }
}
