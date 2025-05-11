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
     * 사용자 ID로 최근 수정된 순서로 자기소개서 조회
     */
    Page<Resume> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);

    /**
     * 사용자 ID로 대표 자기소개서 조회
     */
    Optional<Resume> findByUserIdAndIsMainTrue(Long userId);

    /**
     * 사용자 ID로 포트폴리오가 있는 자기소개서 조회
     */
    @Query("SELECT r FROM Resume r WHERE r.user.id = :userId AND r.portfolio IS NOT NULL AND r.portfolio <> ''")
    List<Resume> findByUserIdAndPortfolioIsNotNull(@Param("userId") Long userId);
}