package aibe.hosik.apply;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/applies")
@RequiredArgsConstructor
public class ApplyController {
  private final ApplyService applyService;

  /**
   * 지원 요청을 처리하는 엔드포인트
   * 예시 요청:
   * POST /api/applies
   * {
   * "userId": 1,
   * "postId": 2,
   * "resumeId": 3
   * }
   */
  @PostMapping
  public void apply(@RequestBody ApplyRequest request) {
    applyService.apply(request.getUserId(), request.getPostId(), request.getResumeId());
  }
}