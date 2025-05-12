package aibe.hosik.resume.dto;

/**
 * 자기소개서 포트폴리오 URL 업데이트 요청 DTO
 */
public record ResumePortfolioRequest(
        String portfolio
) {
    public static ResumePortfolioRequest of(String portfolio) {
        return new ResumePortfolioRequest(portfolio);
    }
}