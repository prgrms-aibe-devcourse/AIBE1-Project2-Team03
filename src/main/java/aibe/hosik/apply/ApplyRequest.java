package aibe.hosik.apply;

/**
 * 지원 요청 DTO
 */
public class ApplyRequest {
    private final Long userId;
    private final Long postId;
    private final Long resumeId;

    private ApplyRequest(Builder builder) {
        this.userId = builder.userId;
        this.postId = builder.postId;
        this.resumeId = builder.resumeId;
    }

    // 접근자 메서드
    public Long getUserId() { return userId; }
    public Long getPostId() { return postId; }
    public Long getResumeId() { return resumeId; }

    // 빌더 클래스
    public static class Builder {
        private Long userId;
        private Long postId;
        private Long resumeId;

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder postId(Long postId) {
            this.postId = postId;
            return this;
        }

        public Builder resumeId(Long resumeId) {
            this.resumeId = resumeId;
            return this;
        }

        public ApplyRequest build() {
            return new ApplyRequest(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // Jackson 역직렬화를 위한 대체 생성자 (프레임워크 호환성)
    public ApplyRequest(Long userId, Long postId, Long resumeId) {
        this.userId = userId;
        this.postId = postId;
        this.resumeId = resumeId;
    }

    // Jackson 역직렬화를 위한 빈 생성자 (프레임워크 호환성)
    private ApplyRequest() {
        this.userId = null;
        this.postId = null;
        this.resumeId = null;
    }
}