package aibe.hosik.apply.controller;

import aibe.hosik.apply.dto.ApplyDetailResponseDTO;
import aibe.hosik.apply.dto.ApplyRequest;
import aibe.hosik.apply.dto.ApplyByResumeSkillResponse;
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

  @SecurityRequirement(name = "JWT")
  @Operation(summary = "모집글 지원", description = "사용자가 모집글에 지원합니다. 자신의 이력서만 사용 가능합니다.")
  @PostMapping
  public ResponseEntity<?> apply(@RequestBody ApplyRequest request, @AuthenticationPrincipal User user) {
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }
    applyService.apply(user.getId(), request.postId(), request.resumeId(), request.reason());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @SecurityRequirement(name = "JWT")
  @Operation(summary = "모집글별 지원서 리스트", description = "특정 모집글에 지원한 사람들의 지원서와 AI 분석을 출력합니다")
  @GetMapping("/post/{postId}/resume-skills")
  public ResponseEntity<List<ApplyByResumeSkillResponse>> getApplyResumeWithSkills(@PathVariable Long postId,@AuthenticationPrincipal  User user) {
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }
    List<ApplyByResumeSkillResponse> result = applyService.getApplyResumeWithSkillsByPostId(postId, user);
    return ResponseEntity.ok(result);
  }

  @SecurityRequirement(name="JWT")
  @Operation(summary = "지원서 상세보기", description = "특정 지원서의 상세 정보를 조회합니다")
  @GetMapping("/{applyId}")
  public ResponseEntity<ApplyDetailResponseDTO> getApplyDetail(
          @PathVariable Long applyId, @AuthenticationPrincipal User user){
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }
    ApplyDetailResponseDTO result = applyService.getApplyDetailByApplyId(applyId, user);
    return ResponseEntity.ok(result);
  }

  @SecurityRequirement(name="JWT")
  @Operation(summary = "지원서 삭제(지원 취소)", description = "사용자가 자신의 지원서를 삭제합니다")
  @DeleteMapping("/{applyId}")
  public ResponseEntity<?> deleteApply(
          @PathVariable Long applyId, @AuthenticationPrincipal User user){
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }
    applyService.deleteApply(applyId, user);
    return ResponseEntity.noContent().build();
  }

  @SecurityRequirement(name="JWT")
  @Operation(summary = "팀원 매칭 선택/취소", description = "모집글 작성자가 지원자를 팀원으로 선택하거나 취소합니다.")
  @PatchMapping("/{applyId}/selection")
  public ResponseEntity<?> updateSelection(
          @PathVariable Long applyId,
          @RequestParam boolean selected,
          @AuthenticationPrincipal User user) {
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }
    applyService.updateIsSelected(applyId, selected, user);
    return ResponseEntity.ok().build();
  }

}