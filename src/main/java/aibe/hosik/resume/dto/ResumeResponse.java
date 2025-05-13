package aibe.hosik.resume.dto;


import aibe.hosik.resume.entity.Resume;

import java.util.List;

public record ResumeResponse(
    Long id,
    String title,
    String content,
    String personality,
    String portfolio,
    boolean isMain,
    List<String> skills,
    Long userId
) {
  public static ResumeResponse from(Resume resume) {
    return new ResumeResponse(
        resume.getId(),
        resume.getTitle(),
        resume.getContent(),
        resume.getPersonality(),
        resume.getPortfolio(),
        resume.isMain(),
        resume.getResumeSkills()
            .stream()
            .map(resumeSkill -> resumeSkill.getSkill().getName())
            .toList(),
        resume.getUser().getId()
    );
  }
}
