package aibe.hosik.profile.dto;

import aibe.hosik.profile.Profile;
import aibe.hosik.resume.dto.ResumeDTO;
import aibe.hosik.resume.entity.Resume;
import aibe.hosik.review.dto.ReviewDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 프로필 조회 응답 DTO
 */
public record ProfileDTO(
        Long id,
        Long userId,
        String userEmail,
        String introduction,
        String image,
        String nickname,
        boolean isPublic,
        ResumeDTO mainResume,
        List<ReviewDTO> receivedReviews
) {
    /**
     * Profile 엔티티로부터 DTO 생성 (기본)
     */
    public static ProfileDTO from(Profile profile) {
        return new ProfileDTO(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getEmail(),
                profile.getIntroduction(),
                profile.getImage(),
                profile.getNickname(),
                profile.isPublic(),
                profile.getMainResume() != null ? ResumeDTO.from(profile.getMainResume()) : null,
                null // 리뷰는 별도로 로드할 때만 포함
        );
    }

    /**
     * 리뷰를 포함한 ProfileDTO 생성
     */
    public static ProfileDTO withReviews(Profile profile, List<ReviewDTO> reviewDTOs) {
        return new ProfileDTO(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getEmail(),
                profile.getIntroduction(),
                profile.getImage(),
                profile.getNickname(),
                profile.isPublic(),
                profile.getMainResume() != null ? ResumeDTO.from(profile.getMainResume()) : null,
                reviewDTOs
        );
    }

    /**
     * 간소화된 프로필 정보만 포함
     */
    public static ProfileDTO summary(Profile profile) {
        return new ProfileDTO(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getEmail(),
                profile.getIntroduction(),
                profile.getImage(),
                profile.getNickname(),
                profile.isPublic(),
                null,
                null
        );
    }
}