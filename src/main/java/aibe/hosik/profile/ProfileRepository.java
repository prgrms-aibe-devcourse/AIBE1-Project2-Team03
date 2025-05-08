package aibe.hosik.profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    // 유저 ID로 프로필 조회
    Optional<Profile> findByUserId(Long userId);

    // 대표 자기소개서와 함께 프로필 조회 (JPQL 사용)
    @Query("SELECT p FROM Profile p LEFT JOIN FETCH p.resumes r WHERE p.user.id = :userId AND (r.isMain = true OR r IS NULL)")
    Optional<Profile> findByUserIdWithMainResume(@Param("userId") Long userId);

    // 리뷰와 함께 프로필 조회
    @Query("SELECT p FROM Profile p LEFT JOIN FETCH p.receivedReviews WHERE p.id = :profileId")
    Optional<Profile> findByIdWithReviews(@Param("profileId") Long profileId);

    // 이메일로 프로필 조회
    @Query("SELECT p FROM Profile p WHERE p.user.email = :email")
    Optional<Profile> findByUserEmail(@Param("email") String email);
}