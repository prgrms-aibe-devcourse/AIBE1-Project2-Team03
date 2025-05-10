package aibe.hosik.apply.controller;

import aibe.hosik.apply.ApplyRequest;
import aibe.hosik.apply.dto.ApplyByResumeSkillResponse;
import aibe.hosik.apply.dto.ApplyResumeResponse;
import aibe.hosik.apply.dto.ApplyUserResponse;
import aibe.hosik.apply.entity.Apply;
import aibe.hosik.apply.service.ApplyService;
import aibe.hosik.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
   *                예시 요청:
   *                POST /api/applies
   *                {
   *                "userId": 1,
   *                "postId": 2,
   *                "resumeId": 3
   *                }
   * @return
   */
  @SecurityRequirement(name = "JWT")
  @Operation(summary = "모집글에 지원", description = "사용자가 모집글에 지원합니다. 자신의 이력서만 사용 가능합니다.")
  @PostMapping
  public ResponseEntity<?> apply(@RequestBody ApplyRequest request, @AuthenticationPrincipal User user) {
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }
    applyService.apply(request.getUserId(), request.getPostId(), request.getResumeId());
    return ResponseEntity.status(HttpStatus.CREATED).build();
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

  /**
   * 특정 모집글에 지원한 사람들의 자기소개서 전문을 조회하는 API
   * (AI 분석용 전체 보기 용도)
   *
   * @param postId 모집글 ID
   * @return 자기소개서 정보 리스트 (ApplyResumeResponse)
   * 예시 요청: GET /api/applies/post/1/resumes
   */
  @GetMapping("/post/{postId}/resumes")
  public List<ApplyResumeResponse> getApplyResumes(@PathVariable Long postId) {
    return applyService.getApplyResumesByPostId(postId);
  }

  /**
   * 특정 모집글에 지원한 사람들의 자기소개서와 스킬 정보를 함께 조회하는 API
   * (AI 분석 및 모아보기 용도)
   */
  @SecurityRequirement(name = "JWT")
  @Operation(summary = "모집글별 지원 소개서 모아보기", description = "특정 모집글에 지원한 사람들 모아보기")
  @GetMapping("/post/{postId}/resume-skills")
  public ResponseEntity<List<ApplyByResumeSkillResponse>> getApplyResumeWithSkills(@PathVariable Long postId,@AuthenticationPrincipal  User user) {
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }
    return ResponseEntity.ok(applyService.getApplyResumeWithSkillsByPostId(postId, user));
  }
}