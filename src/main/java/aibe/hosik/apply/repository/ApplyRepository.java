package aibe.hosik.apply.repository;

import aibe.hosik.apply.entity.Apply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplyRepository extends JpaRepository<Apply, Long> {

    /**
     * 특정 모집글에 대해 선정된 지원자만을 조회하며,
     * 지원자의 프로필 정보도 함께 페치 조인으로 가져온다.
     *
     * @param postId 모집글 ID
     * @return 선정된 지원자 목록 (User + Profile 포함)
     */
    @Query("SELECT a FROM Apply a " +
            "JOIN FETCH a.user u " +
            "JOIN FETCH u.profile p " +
            "WHERE a.post.id = :postId AND a.isSelected = true")
    List<Apply> findWithUserAndProfileByPostId(@Param("postId") Long postId);

    /**
     * 특정 모집글에 대해 선정된 지원자의 수를 반환한다.
     *
     * @param postId 모집글 ID
     * @return 선정된 지원자 수
     */
    int countByPostIdAndIsSelectedTrue(Long postId);

    /**
     * 특정 모집글에 지원한 모든 지원 이력(Apply)을 조회한다.
     *
     * @param postId 모집글 ID
     * @return Apply 리스트
     */
    List<Apply> findByPostId(Long postId);

    /**
     * 특정 모집글에 지원한 지원자들과 그들의 이력서를 함께 조회한다.
     * (AI 분석용 - 자기소개서 요약 등을 위해 사용)
     *
     * @param postId 모집글 ID
     * @return Apply 리스트 (User + Resume 포함)
     */
    @Query("SELECT a FROM Apply a " +
            "JOIN FETCH a.user u " +
            "JOIN FETCH a.resume r " +
            "WHERE a.post.id = :postId")
    List<Apply> findWithUserAndResumeByPostId(@Param("postId") Long postId);


    /**
     * 특정 모집글에 지원한 지원자들의 정보, 이력서, 기술 스킬을 함께 조회한다.
     *
     * @param postId 모집글 ID
     * @return Apply 리스트 (User + Profile + Resume 포함)
     */
    @Query("SELECT a FROM Apply a " +
            "JOIN FETCH a.user u " +
            "JOIN FETCH u.profile p " +
            "JOIN FETCH a.resume r " +
            "WHERE a.post.id = :postId")
    List<Apply> findWithUserResumeAndSkillsByPostId(@Param("postId") Long postId);

    /**
     * 특정 모집글에 지원한 지원자들과 그들의 이력서, 프로필 및 분석 결과를 함께 조회한다.
     * (AI 분석 결과까지 한 번에 조회)
     *
     * @param postId 모집글 ID
     * @return Apply 리스트 (User + Profile + Resume + Analysis 포함)
     */
    @Query("SELECT DISTINCT a FROM Apply a " +
            "JOIN FETCH a.user u " +
            "JOIN FETCH u.profile p " +
            "JOIN FETCH a.resume r " +
            "LEFT JOIN FETCH a.analysis " +
            "WHERE a.post.id = :postId")
    List<Apply> findWithUserResumeAndAnalysisByPostId(@Param("postId") Long postId);

}