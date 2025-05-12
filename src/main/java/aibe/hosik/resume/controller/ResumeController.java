package aibe.hosik.resume;

import aibe.hosik.common.dto.ApiResponse;
import aibe.hosik.common.exception.ResourceNotFoundException;
import aibe.hosik.resume.dto.ResumeRequest;
import aibe.hosik.user.User;
import aibe.hosik.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {
  private final ResumeService resumeService;
  private final UserRepository userRepository;

  // 기존 API 엔드포인트들은 그대로 유지...

  /**
   * 포트폴리오 파일 업로드
   */
  @PostMapping(value = "/{resumeId}/portfolio-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> uploadPortfolioFile(
          @PathVariable Long resumeId,
          @RequestParam("file") MultipartFile file) {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

    try {
      Resume resume = resumeService.updatePortfolioFile(resumeId, user.getId(), file);
      log.info("Uploaded portfolio file for resume ID: {} by user: {}", resumeId, email);
      return ResponseEntity.ok(resume);
    } catch (Exception e) {
      log.error("Error uploading portfolio file for resume ID: {} by user {}: {}",
              resumeId, email, e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  /**
   * 포트폴리오 URL 업데이트 (기존 메서드 유지)
   */
  @PutMapping("/{resumeId}/portfolio")
  public ResponseEntity<?> updatePortfolio(@PathVariable Long resumeId, @RequestBody Map<String, String> portfolioData) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

    try {
      String portfolio = portfolioData.get("portfolio");
      Resume resume = resumeService.updatePortfolio(resumeId, user.getId(), portfolio);

      log.info("Updated portfolio for resume ID: {} by user: {}", resumeId, email);
      return ResponseEntity.ok(resume);
    } catch (Exception e) {
      log.error("Error updating portfolio for resume ID: {} by user {}: {}",
              resumeId, email, e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ApiResponse(false, e.getMessage()));
    }
  }
}