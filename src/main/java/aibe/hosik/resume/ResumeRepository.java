package aibe.hosik.resume;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    /**
     * 사용자 ID로 최근 수정된 순서로 자기소개서 조회 (페이징)
     */
    Page<Resume> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);

    /**
     * 사용자 ID로 최근 수정된 순서로 자기소개서 조회 (제한된 개수)
     * Spring Data JPA는 Top 키워드를 사용하여 자동으로 LIMIT 적용
     */
    @Query("SELECT r FROM Resume r WHERE r.user.id = :userId ORDER BY r.updatedAt DESC")
    List<Resume> findTopByUserIdOrderByUpdatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    /**
     * 사용자 ID로 대표 자기소개서 조회
     */
    Optional<Resume> findByUserIdAndIsMainTrue(Long userId);

    /**
     * 사용자 ID로 포트폴리오가 있는 자기소개서 조회
     */
    @Query("SELECT r FROM Resume r WHERE r.user.id = :userId AND r.portfolio IS NOT NULL AND r.portfolio <> ''")
    List<Resume> findByUserIdAndPortfolioIsNotNull(@Param("userId") Long userId);

    /**
     * 메서드명 통일을 위한 포트폴리오 자기소개서 조회
     */
    @Query("SELECT r FROM Resume r WHERE r.user.id = :userId AND r.portfolio IS NOT NULL AND r.portfolio <> ''")
    List<Resume> findPortfoliosByUserId(@Param("userId") Long userId);

    /**
     * 자기소개서 타이틀로 검색
     */
    @Query("SELECT r FROM Resume r WHERE r.user.id = :userId AND r.title LIKE %:keyword%")
    List<Resume> findByUserIdAndTitleContaining(@Param("userId") Long userId, @Param("keyword") String keyword);
}