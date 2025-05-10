package aibe.hosik.resume.dto;


import aibe.hosik.resume.entity.Resume;

public record ResumeDTO (
    Long id,
    String title,
    boolean isMain
) {
    public static ResumeDTO from(Resume resume){
        return new ResumeDTO(resume.getId(), resume.getTitle(), resume.isMain());
    }
}
