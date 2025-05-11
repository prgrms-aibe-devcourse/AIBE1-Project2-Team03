package aibe.hosik.apply.service;

import aibe.hosik.apply.entity.Apply;
import aibe.hosik.apply.repository.ApplyRepository;
import aibe.hosik.common.exception.AccessDeniedException;
import aibe.hosik.common.exception.ResourceNotFoundException;
import aibe.hosik.post.entity.Post;
import aibe.hosik.post.repository.PostRepository;
import aibe.hosik.resume.Resume;
import aibe.hosik.resume.ResumeRepository;
import aibe.hosik.user.User;
import aibe.hosik.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplyService {

  private final ApplyRepository applyRepository; // Apply 테이블과 통신하는 레포
  private final PostRepository postRepository; // Post 테이블과 통신
  private final ResumeRepository resumeRepository; // Resume 테이블과 통신
  private final UserRepository userRepository;

  /**
   * 사용자가 특정 모집글에 특정 이력서를 가지고 지원하는 기능
   * @param userId 지원자 ID
   * @param postId 모집글 ID
   * @param resumeId 지원자가 선택한 이력서 ID
   * @return 저장된 지원 정보
   */
  @Transactional
  public Apply apply(Long userId, Long postId, Long resumeId) {
    log.info("User {} applying to post {} with resume {}", userId, postId, resumeId);

    Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("모집글을 찾을 수 없습니다: " + postId));

    Resume resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new ResourceNotFoundException("자기소개서를 찾을 수 없습니다: " + resumeId));

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));

    // 본인의 자기소개서인지 확인
    if (!resume.getUser().getId().equals(userId)) {
      throw new AccessDeniedException("본인의 자기소개서만 사용할 수 있습니다.");
    }

    // 이미 지원한 모집글인지 확인
    if (applyRepository.existsByUserIdAndPostId(userId, postId)) {
      throw new IllegalStateException("이미 지원한 모집글입니다.");
    }

    // 마감된 모집글인지 확인
    if (post.isDone()) {
      throw new IllegalStateException("마감된 모집글입니다.");
    }

    Apply apply = Apply.builder() // Apply 엔티티 생성
            .post(post) // 어떤 모집글에
            .user(user) // 누가
            .resume(resume) // 어떤 이력서로
            .isSelected(false) // 기본값은 미선정
            .build();

    return applyRepository.save(apply); // DB에 저장
  }

  /**
   * 지원 취소 기능
   * @param applyId 지원 ID
   * @param userId 사용자 ID (권한 확인용)
   */
  @Transactional
  public void cancelApply(Long applyId, Long userId) {
    log.info("Cancelling apply {} for user {}", applyId, userId);

    Apply apply = applyRepository.findById(applyId)
            .orElseThrow(() -> new ResourceNotFoundException("지원 정보를 찾을 수 없습니다: " + applyId));

    // 본인의 지원인지 확인
    if (!apply.getUser().getId().equals(userId)) {
      throw new AccessDeniedException("본인의 지원만 취소할 수 있습니다.");
    }

    // 이미 선발된 지원은 취소 불가
    if (apply.isSelected()) {
      throw new IllegalStateException("이미 선발된 지원은 취소할 수 없습니다.");
    }

    applyRepository.delete(apply);
    log.info("Apply {} cancelled successfully", applyId);
  }

  /**
   * 사용자의 지원 목록 조회
   * @param userId 사용자 ID
   * @param pageable 페이징 정보
   * @return 지원 목록
   */
  @Transactional(readOnly = true)
  public Page<Apply> getAppliesByUserId(Long userId, Pageable pageable) {
    log.info("Getting applies for user {}", userId);
    return applyRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
  }

  /**
   * 사용자의 프로젝트(매칭 성공) 목록 조회
   * @param userId 사용자 ID
   * @param pageable 페이징 정보
   * @return 매칭 성공된 지원 목록
   */
  @Transactional(readOnly = true)
  public Page<Apply> getSelectedAppliesByUserId(Long userId, Pageable pageable) {
    log.info("Getting selected applies (projects) for user {}", userId);
    return applyRepository.findByUserIdAndIsSelectedTrueOrderByCreatedAtDesc(userId, pageable);
  }

  /**
   * 특정 모집글에 대한 지원 목록 조회
   * @param postId 모집글 ID
   * @param pageable 페이징 정보
   * @return 지원 목록
   */
  @Transactional(readOnly = true)
  public Page<Apply> getAppliesByPostId(Long postId, Pageable pageable) {
    log.info("Getting applies for post {}", postId);
    return applyRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable);
  }

  /**
   * 지원 정보 상세 조회
   * @param applyId 지원 ID
   * @return 지원 정보
   */
  @Transactional(readOnly = true)
  public Apply getApply(Long applyId) {
    log.info("Getting apply {}", applyId);
    return applyRepository.findById(applyId)
            .orElseThrow(() -> new ResourceNotFoundException("지원 정보를 찾을 수 없습니다: " + applyId));
  }
}