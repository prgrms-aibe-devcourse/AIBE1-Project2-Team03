package aibe.hosik.resume;

import aibe.hosik.common.exception.AccessDeniedException;
import aibe.hosik.common.exception.ResourceNotFoundException;
import aibe.hosik.user.User;
import aibe.hosik.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {
  private final ResumeRepository repository;
  private final UserRepository userRepository;
  private final ResumeStorageService resumeStorageService; // 이력서 전용 스토리지 서비스 주입

  /**
   * 새 자기소개서 등록
   */
  @Transactional
  public Resume createResume(Long userId, String title, String content, String personality,
                             String portfolio, boolean isMain) {
    log.info("Creating resume for user ID: {}", userId);

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));

    // 대표 자기소개서로 설정하는 경우, 기존 대표 자기소개서 해제
    if (isMain) {
      resetMainResume(userId);
    }

    Resume resume = Resume.baseBuilder()
            .user(user)
            .title(title)
            .content(content)
            .personality(personality)
            .portfolio(portfolio)
            .isMain(isMain)
            .build();

    return repository.save(resume);
  }

  /**
   * 자기소개서 수정
   */
  @Transactional
  public Resume updateResume(Long resumeId, Long userId, String title, String content,
                             String personality, String portfolio, boolean isMain) {
    log.info("Updating resume ID: {} for user ID: {}", resumeId, userId);

    Resume resume = getResumeWithOwnerCheck(resumeId, userId);

    // 대표 자기소개서로 설정하는 경우, 기존 대표 자기소개서 해제
    if (isMain && !resume.isMain()) {
      resetMainResume(userId);
    }

    // 상태 변경 메서드를 사용하여 엔티티 수정
    resume.updateResume(title, content, personality, portfolio);

    if (isMain) {
      resume.setAsMain();
    } else {
      resume.unsetMain();
    }

    return repository.save(resume);
  }

  /**
   * 포트폴리오 파일 업로드 및 URL 업데이트
   */
  @Transactional
  public Resume updatePortfolioFile(Long resumeId, Long userId, MultipartFile file) throws Exception {
    log.info("Updating portfolio file for resume ID: {} by user ID: {}", resumeId, userId);

    Resume resume = getResumeWithOwnerCheck(resumeId, userId);

    if (file != null && !file.isEmpty()) {
      try {
        // ResumeStorageService를 통한 파일 업로드
        String fileUrl = resumeStorageService.uploadPortfolioFile(file);
        log.info("Portfolio file uploaded to Supabase: {}", fileUrl);

        // 포트폴리오 URL 업데이트
        resume.updatePortfolio(fileUrl);
        return repository.save(resume);
      } catch (Exception e) {
        log.error("Failed to upload portfolio file: {}", e.getMessage());
        throw new Exception("포트폴리오 파일 업로드에 실패했습니다: " + e.getMessage());
      }
    } else {
      throw new IllegalArgumentException("업로드할 파일이 없습니다.");
    }
  }

  /**
   * 자기소개서 포트폴리오 URL 업데이트 (문자열 URL)
   */
  @Transactional
  public Resume updatePortfolio(Long resumeId, Long userId, String portfolio) {
    log.info("Updating portfolio URL for resume ID: {} for user ID: {}", resumeId, userId);

    Resume resume = getResumeWithOwnerCheck(resumeId, userId);
    resume.updatePortfolio(portfolio);

    return repository.save(resume);
  }

  /**
   * 대표 자기소개서로 설정
   */
  @Transactional
  public Resume setAsMainResume(Long resumeId, Long userId) {
    log.info("Setting resume ID: {} as main for user ID: {}", resumeId, userId);

    Resume resume = getResumeWithOwnerCheck(resumeId, userId);

    // 이미 대표 자기소개서인 경우 처리 종료
    if (resume.isMain()) {
      return resume;
    }

    // 기존 대표 자기소개서 해제
    resetMainResume(userId);

    // 대표 자기소개서로 설정
    resume.setAsMain();
    return repository.save(resume);
  }

  /**
   * 자기소개서 삭제
   */
  @Transactional
  public void deleteResume(Long resumeId, Long userId) {
    log.info("Deleting resume ID: {} for user ID: {}", resumeId, userId);

    Resume resume = getResumeWithOwnerCheck(resumeId, userId);
    repository.delete(resume);
  }

  /**
   * 자기소개서 조회
   */
  @Transactional(readOnly = true)
  public Resume getResume(Long resumeId) {
    log.info("Getting resume ID: {}", resumeId);
    return repository.findById(resumeId)
            .orElseThrow(() -> new ResourceNotFoundException("자기소개서를 찾을 수 없습니다: " + resumeId));
  }

  /**
   * 사용자의 자기소개서 목록 조회 (페이징)
   */
  @Transactional(readOnly = true)
  public Page<Resume> getUserResumes(Long userId, Pageable pageable) {
    log.info("Getting resumes for user ID: {}", userId);
    return repository.findByUserIdOrderByUpdatedAtDesc(userId, pageable);
  }

  /**
   * 사용자의 최근 자기소개서 목록 조회 (제한된 개수)
   */
  @Transactional(readOnly = true)
  public List<Resume> getRecentResumes(Long userId, int limit) {
    log.info("Getting recent resumes for user ID: {}", userId);
    // PageRequest를 사용하여 제한된 개수만 조회
    Pageable pageable = PageRequest.of(0, limit);
    return repository.findTopByUserIdOrderByUpdatedAtDesc(userId, pageable);
  }

  /**
   * 사용자의 포트폴리오 자기소개서 목록 조회
   */
  @Transactional(readOnly = true)
  public List<Resume> getUserPortfolios(Long userId) {
    log.info("Getting portfolios for user ID: {}", userId);
    return repository.findByUserIdAndPortfolioIsNotNull(userId);
  }

  /**
   * 대표 자기소개서 조회
   */
  @Transactional(readOnly = true)
  public Resume getMainResume(Long userId) {
    log.info("Getting main resume for user ID: {}", userId);
    return repository.findByUserIdAndIsMainTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("대표 자기소개서가 없습니다."));
  }

  /**
   * 자기소개서 타이틀로 검색
   */
  @Transactional(readOnly = true)
  public List<Resume> searchResumesByTitle(Long userId, String keyword) {
    log.info("Searching resumes by title for user ID: {} with keyword: {}", userId, keyword);
    return repository.findByUserIdAndTitleContaining(userId, keyword);
  }

  // 내부 헬퍼 메서드

  /**
   * 자기소개서 조회 및 소유자 확인
   */
  private Resume getResumeWithOwnerCheck(Long resumeId, Long userId) {
    Resume resume = repository.findById(resumeId)
            .orElseThrow(() -> new ResourceNotFoundException("자기소개서를 찾을 수 없습니다: " + resumeId));

    if (!resume.getUser().getId().equals(userId)) {
      throw new AccessDeniedException("이 자기소개서에 접근할 권한이 없습니다.");
    }

    return resume;
  }

  /**
   * 기존 대표 자기소개서 해제
   */
  private void resetMainResume(Long userId) {
    repository.findByUserIdAndIsMainTrue(userId).ifPresent(mainResume -> {
      mainResume.unsetMain();
      repository.save(mainResume);
    });
  }
}