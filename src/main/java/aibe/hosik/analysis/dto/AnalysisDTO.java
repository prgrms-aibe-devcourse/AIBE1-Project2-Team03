package aibe.hosik.analysis.dto;

import aibe.hosik.analysis.entity.Analysis;
import aibe.hosik.apply.entity.Apply;

/**
 * AI 분석 결과 DTO - 요청과 응답에 모두 사용
 */
public record AnalysisDTO(
        Long id,
        String result,      // 분석 결과 (AI가 생성한 추천 이유)
        String summary,     // 자기소개서 요약
        int score,          // 최종 점수
        Long applyId        // 연결된 지원 ID
) {
    /**
     * Analysis Entity에서 DTO로 변환
     */
    public static AnalysisDTO from(Analysis analysis) {
        return new AnalysisDTO(
                analysis.getId(),
                analysis.getResult(),
                analysis.getSummary(),
                analysis.getScore(),
                analysis.getApply().getId()
        );
    }

    /**
     * DTO를 Analysis Entity로 변환
     */
    public Analysis toEntity(Apply apply) {
        return Analysis.builder()
                .apply(apply)
                .result(this.result)
                .summary(this.summary)
                .score(this.score)
                .build();
    }

}