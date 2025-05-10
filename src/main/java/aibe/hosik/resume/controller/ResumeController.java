package aibe.hosik.resume.controller;

import aibe.hosik.resume.dto.ResumeDTO;
import aibe.hosik.resume.service.ResumeService;
import aibe.hosik.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {
  private final ResumeService resumeService;

  @GetMapping("/my-resumes")
  @SecurityRequirement(name = "JWT")
  @Operation(summary = "내 이력서 목록 조회", description = "로그인한 사용자의 이력서 목록을 조회합니다.")
  public ResponseEntity<List<ResumeDTO>> getMyResumes(@AuthenticationPrincipal User user) {
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }

    List<ResumeDTO> resumes = resumeService.getResumeByUserId(user.getId());
    return ResponseEntity.ok(resumes);
  }
}
