package aibe.hosik.resume.dto;

import aibe.hosik.resume.entity.Resume;
import aibe.hosik.user.User;

/**
 * 자기소개서 요청 DTO
 */
public record ResumeRequest(
        String title,
        String content,
        String personality,
        String portfolio,
        boolean isMain
) {
    /**
     * 기본값을 가진 정적 팩토리 메서드
     */
    public static ResumeRequest of(String title, String content) {
        return new ResumeRequest(title, content, null, null, false);
    }

    /**
     * Resume 엔티티 생성
     */
    public Resume toEntity(User user) {
        return Resume.baseBuilder()
                .user(user)
                .title(title())
                .content(content())
                .personality(personality())
                .portfolio(portfolio())
                .isMain(isMain())
                .build();
    }

    /**
     * 부분적 업데이트를 위한 Builder 메서드
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 클래스
     */
    public static class Builder {
        private String title;
        private String content;
        private String personality;
        private String portfolio;
        private boolean isMain;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder personality(String personality) {
            this.personality = personality;
            return this;
        }

        public Builder portfolio(String portfolio) {
            this.portfolio = portfolio;
            return this;
        }

        public Builder isMain(boolean isMain) {
            this.isMain = isMain;
            return this;
        }

        public ResumeRequest build() {
            return new ResumeRequest(title, content, personality, portfolio, isMain);
        }
    }
}