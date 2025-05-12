package aibe.hosik.resume.dto;

import aibe.hosik.resume.entity.Resume;
import java.time.LocalDateTime;

/**
 * 자기소개서 조회 응답 DTO
 */
public record ResumeDTO(
        Long id,
        String title,
        String content,
        String personality,
        String portfolio,
        boolean isMain,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Resume 엔티티로부터 DTO 생성
     */
    public static ResumeDTO from(Resume resume) {
        return new ResumeDTO(
                resume.getId(),
                resume.getTitle(),
                resume.getContent(),
                resume.getPersonality(),
                resume.getPortfolio(),
                resume.isMain(),
                resume.getCreatedAt(),
                resume.getUpdatedAt()
        );
    }

    /**
     * 요약 정보만 포함한 간소화된 DTO 생성
     */
    public static ResumeDTO summary(Resume resume) {
        return new ResumeDTO(
                resume.getId(),
                resume.getTitle(),
                null, // 내용 제외
                null, // 성향 제외
                resume.getPortfolio(),
                resume.isMain(),
                resume.getCreatedAt(),
                resume.getUpdatedAt()
        );
    }
}