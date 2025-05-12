package aibe.hosik.resume.dto;


import aibe.hosik.resume.entity.Resume;

public record ResumeResponse(
    Long id,
    String title,
    String content,
    String personality,
    String portfolio,
    boolean isMain
) {
  public static ResumeResponse from(Resume resume) {
    return new ResumeResponse(
        resume.getId(),
        resume.getTitle(),
        resume.getContent(),
        resume.getPersonality(),
        resume.getPortfolio(),
        resume.isMain()
    );
  }
}
