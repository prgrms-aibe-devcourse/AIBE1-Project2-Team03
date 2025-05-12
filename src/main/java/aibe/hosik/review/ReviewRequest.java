package aibe.hosik.review.dto;

/**
 * 리뷰 작성 요청 DTO
 */
public record ReviewRequest(
        String content
) {
    public static ReviewRequest of(String content) {
        return new ReviewRequest(content);
    }
}