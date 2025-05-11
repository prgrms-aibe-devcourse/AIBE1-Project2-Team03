package aibe.hosik.resume;

import aibe.hosik.common.exception.AccessDeniedException;
import aibe.hosik.common.exception.ResourceNotFoundException;
import aibe.hosik.user.User;
import aibe.hosik.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {
  private final ResumeRepository repository;
  private final UserRepository userRepository;

  /**
   * 새 자기소개서 등록
   *
   * @param userId      사용자 ID
   * @param title       자기소개서 제목
   * @param content     자기소개서 내용
   * @param personality 성격 특성
   * @param portfolio   포트폴리오 링크
   * @param isMain      대표 자기소개서 여부
   * @return 저장된 자기소개서
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

    Resume resume = Resume.builder()
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
   *
   * @param resumeId    자기소개서 ID
   * @param userId      사용자 ID (권한 확인용)
   * @param title       자기소개서 제목
   * @param content     자기소개서 내용
   * @param personality 성격 특성
   * @param portfolio   포트폴리오 링크
   * @param isMain      대표 자기소개서 여부
   * @return 수정된 자기소개서
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

    // 엔티티 수정을 위한 새 인스턴스 생성 (불변성 패턴)
    Resume updatedResume = Resume.builder()
            .id(resumeId)
            .user(resume.getUser())
            .title(title)
            .content(content)
            .personality(personality)
            .portfolio(portfolio)
            .isMain(isMain)
            .build();

    return repository.save(updatedResume);
  }

  /**
   * 대표 자기소개서로 설정
   *
   * @param resumeId 자기소개서 ID
   * @param userId   사용자 ID (권한 확인용)
   * @return 대표로 설정된 자기소개서
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
    Resume mainResume = Resume.builder()
            .id(resumeId)
            .user(resume.getUser())
            .title(resume.getTitle())
            .content(resume.getContent())
            .personality(resume.getPersonality())
            .portfolio(resume.getPortfolio())
            .isMain(true)
            .build();

    return repository.save(mainResume);
  }

  /**
   * 자기소개서 삭제
   *
   * @param resumeId 자기소개서 ID
   * @param userId   사용자 ID (권한 확인용)
   */
  @Transactional
  public void deleteResume(Long resumeId, Long userId) {
    log.info("Deleting resume ID: {} for user ID: {}", resumeId, userId);

    Resume resume = getResumeWithOwnerCheck(resumeId, userId);
    repository.delete(resume);
  }

  /**
   * 자기소개서 조회
   *
   * @param resumeId 자기소개서 ID
   * @return 자기소개서
   */
  @Transactional(readOnly = true)
  public Resume getResume(Long resumeId) {
    log.info("Getting resume ID: {}", resumeId);
    return repository.findById(resumeId)
            .orElseThrow(() -> new ResourceNotFoundException("자기소개서를 찾을 수 없습니다: " + resumeId));
  }

  /**
   * 사용자의 자기소개서 목록 조회
   *
   * @param userId   사용자 ID
   * @param pageable 페이징 정보
   * @return 자기소개서 목록
   */
  @Transactional(readOnly = true)
  public Page<Resume> getUserResumes(Long userId, Pageable pageable) {
    log.info("Getting resumes for user ID: {}", userId);
    return repository.findByUserIdOrderByUpdatedAtDesc(userId, pageable);
  }

  /**
   * 사용자의 포트폴리오 자기소개서 목록 조회
   *
   * @param userId 사용자 ID
   * @return 포트폴리오가 있는 자기소개서 목록
   */
  @Transactional(readOnly = true)
  public List<Resume> getUserPortfolios(Long userId) {
    log.info("Getting portfolios for user ID: {}", userId);
    return repository.findByUserIdAndPortfolioIsNotNull(userId);
  }

  /**
   * 대표 자기소개서 조회
   *
   * @param userId 사용자 ID
   * @return 대표 자기소개서
   */
  @Transactional(readOnly = true)
  public Resume getMainResume(Long userId) {
    log.info("Getting main resume for user ID: {}", userId);
    return repository.findByUserIdAndIsMainTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("대표 자기소개서가 없습니다."));
  }

  // 내부 헬퍼 메서드

  /**
   * 자기소개서 조회 및 소유자 확인
   *
   * @param resumeId 자기소개서 ID
   * @param userId   사용자 ID
   * @return 자기소개서
   * @throws ResourceNotFoundException 자기소개서를 찾을 수 없는 경우
   * @throws AccessDeniedException     접근 권한이 없는 경우
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
   *
   * @param userId 사용자 ID
   */
  private void resetMainResume(Long userId) {
    repository.findByUserIdAndIsMainTrue(userId).ifPresent(mainResume -> {
      Resume updated = Resume.builder()
              .id(mainResume.getId())
              .user(mainResume.getUser())
              .title(mainResume.getTitle())
              .content(mainResume.getContent())
              .personality(mainResume.getPersonality())
              .portfolio(mainResume.getPortfolio())
              .isMain(false)
              .build();

      repository.save(updated);
    });
  }
}