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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {
  private final ResumeService resumeService;
  private final UserRepository userRepository;

  /**
   * 자기소개서 등록
   */
  @PostMapping
  public ResponseEntity<?> createResume(@RequestBody ResumeRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

    try {
      Resume resume = resumeService.createResume(
              user.getId(),
              request.getTitle(),
              request.getContent(),
              request.getPersonality(),
              request.getPortfolio(),
              request.isMain()
      );

      log.info("Created resume ID: {} for user: {}", resume.getId(), email);
      return ResponseEntity.status(HttpStatus.CREATED).body(resume);
    } catch (Exception e) {
      log.error("Error creating resume for user {}: {}", email, e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  /**
   * 자기소개서 수정
   */
  @PutMapping("/{resumeId}")
  public ResponseEntity<?> updateResume(@PathVariable Long resumeId, @RequestBody ResumeRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

    try {
      Resume resume = resumeService.updateResume(
              resumeId,
              user.getId(),
              request.getTitle(),
              request.getContent(),
              request.getPersonality(),
              request.getPortfolio(),
              request.isMain()
      );

      log.info("Updated resume ID: {} for user: {}", resumeId, email);
      return ResponseEntity.ok(resume);
    } catch (Exception e) {
      log.error("Error updating resume ID: {} for user {}: {}", resumeId, email, e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  /**
   * 자기소개서 삭제
   */
  @DeleteMapping("/{resumeId}")
  public ResponseEntity<?> deleteResume(@PathVariable Long resumeId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

    try {
      resumeService.deleteResume(resumeId, user.getId());

      log.info("Deleted resume ID: {} for user: {}", resumeId, email);
      return ResponseEntity.ok(new ApiResponse(true, "자기소개서가 삭제되었습니다."));
    } catch (Exception e) {
      log.error("Error deleting resume ID: {} for user {}: {}", resumeId, email, e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  /**
   * 자기소개서 조회
   */
  @GetMapping("/{resumeId}")
  public ResponseEntity<?> getResume(@PathVariable Long resumeId) {
    try {
      Resume resume = resumeService.getResume(resumeId);

      log.info("Retrieved resume ID: {}", resumeId);
      return ResponseEntity.ok(resume);
    } catch (Exception e) {
      log.error("Error retrieving resume ID: {}: {}", resumeId, e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  /**
   * 사용자의 자기소개서 목록 조회
   */
  @GetMapping("/user/{userId}")
  public ResponseEntity<?> getUserResumes(
          @PathVariable Long userId,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size) {

    try {
      Pageable pageable = PageRequest.of(page, size);
      Page<Resume> resumes = resumeService.getUserResumes(userId, pageable);

      log.info("Retrieved resumes for user ID: {}, total: {}", userId, resumes.getTotalElements());
      return ResponseEntity.ok(resumes);
    } catch (Exception e) {
      log.error("Error retrieving resumes for user ID: {}: {}", userId, e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  /**
   * 대표 자기소개서로 설정
   */
  @PutMapping("/{resumeId}/main")
  public ResponseEntity<?> setAsMainResume(@PathVariable Long resumeId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

    try {
      Resume resume = resumeService.setAsMainResume(resumeId, user.getId());

      log.info("Set resume ID: {} as main for user: {}", resumeId, email);
      return ResponseEntity.ok(resume);
    } catch (Exception e) {
      log.error("Error setting resume ID: {} as main for user {}: {}", resumeId, email, e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  /**
   * 포트폴리오 목록 조회
   */
  @GetMapping("/user/{userId}/portfolios")
  public ResponseEntity<?> getUserPortfolios(@PathVariable Long userId) {
    try {
      List<Resume> portfolios = resumeService.getUserPortfolios(userId);

      log.info("Retrieved portfolios for user ID: {}, total: {}", userId, portfolios.size());
      return ResponseEntity.ok(portfolios);
    } catch (Exception e) {
      log.error("Error retrieving portfolios for user ID: {}: {}", userId, e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ApiResponse(false, e.getMessage()));
    }
  }
}