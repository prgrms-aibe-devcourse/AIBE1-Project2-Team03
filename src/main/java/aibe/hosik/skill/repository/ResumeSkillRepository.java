package aibe.hosik.skill.repository;


import aibe.hosik.skill.entity.ResumeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResumeSkillRepository extends JpaRepository<ResumeSkill, Long> {
    /**
     * 이력서 ID로 관련 ResumeSkill 엔티티들을 조회합니다.
     */
    @Query("SELECT rs FROM ResumeSkill rs JOIN FETCH rs.skill WHERE rs.resume.id = :resumeId")
    List<ResumeSkill> findByResumeId(@Param("resumeId") Long resumeId);
}