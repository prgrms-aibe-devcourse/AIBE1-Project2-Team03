package aibe.hosik.apply;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplyRequest {
    private Long userId;
    private Long postId;
    private Long resumeId;
}
