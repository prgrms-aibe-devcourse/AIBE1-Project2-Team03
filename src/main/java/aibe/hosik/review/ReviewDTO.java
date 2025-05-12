package aibe.hosik.review.dto;

import aibe.hosik.review.Review;

import java.time.LocalDateTime;

/**
 * 리뷰 응답 DTO
 */
public record ReviewDTO(
        Long id,
        String content,
        Long reviewerId,
        String reviewerEmail,
        Long revieweeId,
        String revieweeEmail,
        LocalDateTime createdAt
) {
    /**
     * Review 엔티티로부터 DTO 생성
     */
    public static ReviewDTO from(Review review) {
        return new ReviewDTO(
                review.getId(),
                review.getContent(),
                review.getReviewer().getId(),
                review.getReviewer().getEmail(),
                review.getReviewee().getId(),
                review.getReviewee().getEmail(),
                review.getCreatedAt()
        );
    }
}