package aibe.hosik.apply.dto;

import aibe.hosik.analysis.entity.Analysis;
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

        Integer aiScore,
        String aiReason,
        String aiSummary
) {
    public static ApplyByResumeSkillResponse from(Apply apply, List<String> skills, Analysis analysis) {
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
                analysis != null ? analysis.getScore() : null,
                analysis != null ? analysis.getResult() : null,
                analysis != null ? analysis.getSummary() : null
        );
    }
}
