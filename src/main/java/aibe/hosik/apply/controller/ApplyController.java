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
   * 사용자가 모집글에 지원하는 API
   *
   * @param request 지원 요청 정보 (userId, postId, resumeId)
   * 예시 요청:
   * POST /api/applies
   * {
   *   "userId": 1,
   *   "postId": 2,
   *   "resumeId": 3
   * }
   */
  @PostMapping
  public void apply(@RequestBody ApplyRequest request) {
    applyService.apply(request.getUserId(), request.getPostId(), request.getResumeId());
  }

  /**
   * 특정 모집글에 지원한 모든 Apply 엔티티(지원 이력) 목록을 조회하는 API
   *
   * @param postId 모집글 ID
   * @return 해당 모집글에 지원한 Apply 객체 리스트
   * 예시 요청: GET /api/applies/post/1
   */
  @GetMapping("/post/{postId}")
  public List<Apply> getAppliesByPostId(@PathVariable Long postId) {
    return applyService.getAppliesByPostId(postId);
  }

  /**
   * 특정 모집글에 지원한 지원자들의 상세 정보를 조회하는 API
   *
   * @param postId 모집글 ID
   * @return 지원자 정보 목록 (ApplyUserResponse 리스트)
   * 예시 요청: GET /api/applies/post/1/users
   */
  @GetMapping("/post/{postId}/users")
  public List<ApplyUserResponse> getApplyUsersByPost(@PathVariable Long postId) {
    return applyService.getApplyUserResponsesByPostId(postId);
  }
}