package aibe.hosik.apply.dto;

/**
 * 모집글에 지원한 사용자의 자기소개서 요약 정보를 담는 DTO.
 * AI 분석을 위해 사용됩니다.
 *
 * @param userId       사용자 ID
 * @param resumeId     이력서 ID
 * @param introduction 자기소개 내용
 * @param personality  성향 정보
 */
public record ApplyResumeResponse(
        Long userId,
        Long resumeId,
        String introduction,
        String personality
) {}