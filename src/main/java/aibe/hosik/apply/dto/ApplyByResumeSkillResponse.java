package aibe.hosik.apply.dto;

import aibe.hosik.analysis.entity.Analysis;
import aibe.hosik.apply.entity.Apply;
import aibe.hosik.profile.Profile;
import aibe.hosik.resume.dto.ResumeResponse;
import aibe.hosik.resume.entity.Resume;
import aibe.hosik.user.User;
import lombok.Builder;

import java.util.List;

@Builder
public record ApplyByResumeSkillResponse(
        Long applyId,
        Long userId,
        ResumeResponse resume,
        String nickname,
        String profileImage,
        boolean isSelected,

        Integer aiScore,
        String aiReason,
        String aiSummary
) {
    public static ApplyByResumeSkillResponse from(Apply apply, List<String> skills, Analysis analysis) {
        User user = apply.getUser();
        Profile profile = user.getProfile();
        ResumeResponse resume = ResumeResponse.from(  apply.getResume());


        return ApplyByResumeSkillResponse.builder()
            .applyId(apply.getId())
            .userId(user.getId())
            .resume(resume)
            .nickname(profile.getNickname())
            .profileImage(profile.getImage())
            .isSelected(apply.isSelected())
            .aiScore(analysis != null ? analysis.getScore() : null)
            .aiReason(analysis != null ? analysis.getResult() : null)
            .aiSummary(analysis != null ? analysis.getSummary() : null)
            .build();
    }
}
