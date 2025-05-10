package aibe.hosik.apply.dto;

import aibe.hosik.apply.entity.Apply;
import aibe.hosik.profile.Profile;
import aibe.hosik.resume.entity.Resume;
import aibe.hosik.user.User;

import java.util.List;

public record ApplyByResumeSkillResponse(
        Long userId,
        Long resumeId,
        String nickname,
        String profileImage,
        String introduction,
        String personality,
        String portfolioUrl,
        List<String> skills,
        // TODO : AI 관련 필드 (추후 추가 예정)
        Integer aiScore,
        String aiReason,
        String aiSummary
) {
    public static ApplyByResumeSkillResponse from(Apply apply, List<String> skills) {
        User user = apply.getUser();
        Profile profile = user.getProfile();
        Resume resume = apply.getResume();

        return new ApplyByResumeSkillResponse(
                user.getId(),
                resume.getId(),
                profile.getNickname(),
                profile.getImage(),
                profile.getIntroduction(),
                resume.getTitle(),
                resume.getPortfolio(),
                skills,
                null, // AI 점수 (현재는 null)
                null, // AI 추천
                null  // AI 자기소개서 요약 (현재는 null)
        );
    }
}
