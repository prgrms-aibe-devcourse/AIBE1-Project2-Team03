package aibe.hosik.apply.controller;

import aibe.hosik.apply.ApplyRequest;
import aibe.hosik.apply.dto.ApplyUserResponse;
import aibe.hosik.apply.entity.Apply;
import aibe.hosik.apply.service.ApplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/applies")
@RequiredArgsConstructor
public class ApplyController {
  private final ApplyService applyService;

  /**
//   * 지원 요청을 처리하는 엔드포인트
   * 예시 요청:
   * POST /api/applies
   * {
   * "userId": 1 ,
   * "postId": 2,
   * "resumeId": 3
   * }
   */
  @PostMapping
  public void apply(@RequestBody ApplyRequest request) {
    applyService.apply(request.getUserId(), request.getPostId(), request.getResumeId());
  }

  /**
   * 특정 모집글에 지원한 지원자 목록 조회
   * GET /api/applies/post/{postId}
   */
  @GetMapping("/post/{postId}")
  public List<Apply> getAppliesByPostId(@PathVariable Long postId) {
    return applyService.getAppliesByPostId(postId);
  }

  @GetMapping("/post/{postId}/users")
  public List<ApplyUserResponse> getApplyUsersByPost(@PathVariable Long postId) {
    return applyService.getApplyUserResponsesByPostId(postId);
  }
}