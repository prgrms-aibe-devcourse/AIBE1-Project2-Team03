package aibe.hosik.apply.dto;

/**
 * 지원자 정보를 담는 응답 DTO.
 * 모집자가 지원자 목록을 조회할 때 사용된다.
 *
 * @param userId 지원자의 사용자 ID
 * @param email 지원자의 이메일 주소
 * @param nickname 지원자의 닉네임
 * @param profileImageUrl 지원자의 프로필 이미지 URL
 * @param introduction 지원자의 자기소개
 * @param portfolioUrl 지원자의 포트폴리오 링크
 * @param personality 지원자의 성향 정보
 */
public record ApplyUserResponse(
        Long userId,
        String email,
        String nickname,
        String profileImageUrl,
        String introduction,
        String portfolioUrl,
        String personality
) {}