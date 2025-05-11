package aibe.hosik.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 프로필 리뷰 목록 조회
     */
    @Query("SELECT r FROM Review r WHERE r.reviewee.id = :revieweeId AND r.reviewType = 'PROFILE_REVIEW' ORDER BY r.createdAt DESC")
    List<Review> findProfileReviewsByRevieweeId(@Param("revieweeId") Long revieweeId);

    /**
     * 특정 프로젝트의 피어리뷰 목록 조회
     */
    @Query("SELECT r FROM Review r WHERE r.apply.id = :applyId AND r.reviewType = 'PEER_REVIEW' ORDER BY r.createdAt DESC")
    List<Review> findPeerReviewsByApplyId(@Param("applyId") Long applyId);

    /**
     * 사용자가 특정 프로젝트에서 받은 피어리뷰 목록
     */
    @Query("SELECT r FROM Review r WHERE r.reviewee.id = :revieweeId AND r.apply.id = :applyId AND r.reviewType = 'PEER_REVIEW'")
    List<Review> findPeerReviewsByRevieweeAndApply(@Param("revieweeId") Long revieweeId, @Param("applyId") Long applyId);

    /**
     * 사용자가 작성한 모든 리뷰 조회 (프로필 리뷰 + 피어리뷰)
     */
    @Query("SELECT r FROM Review r WHERE r.reviewer.id = :reviewerId ORDER BY r.createdAt DESC")
    List<Review> findAllByReviewerId(@Param("reviewerId") Long reviewerId);

    /**
     * 사용자가 받은 모든 리뷰 조회 (프로필 리뷰 + 피어리뷰)
     */
    @Query("SELECT r FROM Review r WHERE r.reviewee.id = :revieweeId ORDER BY r.createdAt DESC")
    List<Review> findAllByRevieweeId(@Param("revieweeId") Long revieweeId);

    /**
     * 특정 사용자가 특정 프로젝트에서 다른 사용자에게 이미 리뷰를 작성했는지 확인
     */
    @Query("SELECT r FROM Review r WHERE r.reviewer.id = :reviewerId AND r.reviewee.id = :revieweeId AND r.apply.id = :applyId AND r.reviewType = 'PEER_REVIEW'")
    Optional<Review> findPeerReviewByReviewerAndRevieweeAndApply(
            @Param("reviewerId") Long reviewerId,
            @Param("revieweeId") Long revieweeId,
            @Param("applyId") Long applyId
    );
}