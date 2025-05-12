package aibe.hosik.resume.controller;

import aibe.hosik.resume.dto.ResumeRequest;
import aibe.hosik.resume.entity.Resume;
import aibe.hosik.resume.service.ResumeService;
import aibe.hosik.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@Tag(name = "Resume", description = "자기소개서 API") // Swagger Tag
public class ResumeController {
  private final ResumeService resumeService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "자기소개서 생성")
  @ResponseStatus(HttpStatus.CREATED)
  public void createResume(
      @RequestPart ResumeRequest request,
      @RequestParam("file") MultipartFile file,
      @AuthenticationPrincipal User user
  ) {
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }

    resumeService.createResume(request, file, user);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "자기소개서 삭제")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteResume(
      @PathVariable("id") Long resumeId,
      @AuthenticationPrincipal User user
  ) {
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }

    resumeService.deleteResume(resumeId, user);
  }
}