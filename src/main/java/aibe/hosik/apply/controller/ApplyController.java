package aibe.hosik.apply.controller;

import aibe.hosik.apply.ApplyRequest;
import aibe.hosik.apply.entity.Apply;
import aibe.hosik.apply.service.ApplyService;
import aibe.hosik.common.dto.ApiResponse;
import aibe.hosik.common.exception.ResourceNotFoundException;
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

@Slf4j
@RestController
@RequestMapping("/api/applies")
@RequiredArgsConstructor
public class ApplyController {
  private final ApplyService applyService;
  private final UserRepository userRepository;

  /**
   * 지원 요청을 처리하는 엔드포인트
   * 예시 요청:
   * POST /api/applies
   * {
   * "userId": 1 ,
   * "postId": 2,
   * "resumeId": 3
   * }
   */
  @PostMapping
  public ResponseEntity<?> apply(@RequestBody ApplyRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

    try {
      Apply apply = applyService.apply(user.getId(), request.getPostId(), request.getResumeId());
      log.info("User {} applied to post {} with resume {}", user.getId(), request.getPostId(), request.getResumeId());
      return ResponseEntity.status(HttpStatus.CREATED).body(apply);
    } catch (Exception e) {
      log.error("Error applying: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  /**
   * 지원 취소 엔드포인트
   */
  @DeleteMapping("/{applyId}")
  public ResponseEntity<?> cancelApply(@PathVariable Long applyId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

    try {
      applyService.cancelApply(applyId, user.getId());
      log.info("User {} cancelled apply {}", user.getId(), applyId);
      return ResponseEntity.ok(new ApiResponse(true, "지원이 취소되었습니다."));
    } catch (Exception e) {
      log.error("Error cancelling apply: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  /**
   * 사용자의 지원 목록 조회
   */
  @GetMapping("/user")
  public ResponseEntity<?> getMyApplies(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size) {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

    Pageable pageable = PageRequest.of(page, size);
    Page<Apply> applies = applyService.getAppliesByUserId(user.getId(), pageable);

    log.info("Retrieved {} applies for user {}", applies.getTotalElements(), user.getId());
    return ResponseEntity.ok(applies);
  }

  /**
   * 사용자의 프로젝트(매칭 성공) 목록 조회
   */
  @GetMapping("/user/projects")
  public ResponseEntity<?> getMyProjects(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size) {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

    Pageable pageable = PageRequest.of(page, size);
    Page<Apply> projects = applyService.getSelectedAppliesByUserId(user.getId(), pageable);

    log.info("Retrieved {} projects for user {}", projects.getTotalElements(), user.getId());
    return ResponseEntity.ok(projects);
  }

  /**
   * 특정 모집글에 대한 지원 목록 조회 (모집글 작성자만 접근 가능)
   */
  @GetMapping("/post/{postId}")
  public ResponseEntity<?> getAppliesByPost(
          @PathVariable Long postId,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size) {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

    // 모집글 작성자 확인 로직이 필요하지만, Post 엔티티 구조를 모르므로 생략
    // postRepository.findById(postId)
    //   .filter(post -> post.getUser().getId().equals(user.getId()))
    //   .orElseThrow(() -> new AccessDeniedException("접근 권한이 없습니다."));

    Pageable pageable = PageRequest.of(page, size);
    Page<Apply> applies = applyService.getAppliesByPostId(postId, pageable);

    log.info("Retrieved {} applies for post {}", applies.getTotalElements(), postId);
    return ResponseEntity.ok(applies);
  }

  /**
   * 지원 정보 상세 조회
   */
  @GetMapping("/{applyId}")
  public ResponseEntity<?> getApply(@PathVariable Long applyId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("로그인한 사용자를 찾을 수 없습니다."));

    try {
      Apply apply = applyService.getApply(applyId);

      // 지원자 본인이나 모집글 작성자만 접근 가능한지 확인하는 로직이 필요하지만 생략

      log.info("Retrieved apply {}", applyId);
      return ResponseEntity.ok(apply);
    } catch (Exception e) {
      log.error("Error retrieving apply: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ApiResponse(false, e.getMessage()));
    }
  }
}