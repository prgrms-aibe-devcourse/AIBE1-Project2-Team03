package aibe.hosik.apply.repository;

import aibe.hosik.apply.entity.Apply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplyRepository extends JpaRepository<Apply, Long> {
    @Query("SELECT a FROM Apply a " +
            "JOIN FETCH a.user u " +
            "JOIN FETCH u.profile p " +
            "WHERE a.post.id = :postId AND a.isSelected = true")
    List<Apply> findWithUserAndProfileByPostId(@Param("postId") Long postId);

    int countByPostIdAndIsSelectedTrue(Long postId);

    /**
     * 사용자 ID와 모집글 ID로 지원 정보 존재 여부 확인
     */
    boolean existsByUserIdAndPostId(Long userId, Long postId);

    /**
     * 사용자 ID로 지원 목록 조회 (최신순)
     */
    Page<Apply> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 사용자 ID로 매칭된 지원 목록 조회 (최신순)
     */
    Page<Apply> findByUserIdAndIsSelectedTrueOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 모집글 ID로 지원 목록 조회 (최신순)
     */
    Page<Apply> findByPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable);
}