package aibe.hosik.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    /**
     * 특정 사용자가 작성한 리뷰 목록 조회
     */
    Page<Review> findByReviewerId(Long reviewerId, Pageable pageable);

    /**
     * 특정 사용자가 받은 리뷰 목록 조회
     */
    Page<Review> findByRevieweeId(Long revieweeId, Pageable pageable);
}