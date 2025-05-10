package aibe.hosik.apply.service;

import aibe.hosik.apply.dto.ApplyResumeResponse;
import aibe.hosik.apply.dto.ApplyUserResponse;
import aibe.hosik.apply.entity.Apply;
import aibe.hosik.apply.repository.ApplyRepository;
import aibe.hosik.post.entity.Post;
import aibe.hosik.post.repository.PostRepository;
import aibe.hosik.resume.Resume;
import aibe.hosik.resume.ResumeRepository;
import aibe.hosik.user.User;
import aibe.hosik.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplyService {

  private final ApplyRepository applyRepository; // Apply 테이블과 통신하는 레포
  private final PostRepository postRepository; // Post 테이블과 통신
  private final ResumeRepository resumeRepository; // Resume 테이블과 통신
  private final UserRepository userRepository; // User 테이블과 통신
  /**
   * 사용자가 특정 모집글에 특정 이력서를 가지고 지원하는 기능
   * @param userId 지원자 ID
   * @param postId 모집글 ID
   * @param resumeId 지원자가 선택한 이력서 ID
   */
  public void apply(Long userId, Long postId, Long resumeId) {
    Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found")); // postId로 모집글 조회

    Resume resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new IllegalArgumentException("Resume not found")); // resumeId로 이력서 조회

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    Apply apply = Apply.builder() // Apply 엔티티 생성
            .post(post) // 어떤 모집글에
            .user(user) // 누가
            .resume(resume) // 어떤 이력서로
            .isSelected(false) // 기본값은 미선정
            .build();

    applyRepository.save(apply); // DB에 저장
  }
  /**
   * 특정 모집글에 지원한 모든 Apply 객체를 조회하는 기능
   * @param postId 모집글 ID
   * @return Apply 리스트
   */
  public List<Apply> getAppliesByPostId(Long postId) {
    return applyRepository.findByPostId(postId);
  }

  /**
   * 특정 모집글에 지원한 지원자 정보를 요약 형태로 조회하는 기능
   *
   * @param postId 모집글 ID
   * @return ApplyUserResponse 리스트
   */
  public List<ApplyUserResponse> getApplyUserResponsesByPostId(Long postId) {
    List<Apply> applies = applyRepository.findWithUserAndProfileByPostId(postId);
    return applies.stream()
            .map(apply -> {
              String personality = apply.getResume().getPersonality();
              String portfolio = apply.getResume().getPortfolio();
              return new ApplyUserResponse(
                      apply.getUser().getId(),
                      apply.getUser().getEmail(),
                      apply.getUser().getProfile().getNickname(),
                      apply.getUser().getProfile().getImage(),
                      apply.getUser().getProfile().getIntroduction(),
                      personality,
                      portfolio
              );
            })
            .toList();
  }
  /**
   * 특정 모집글에 지원한 사람들의 자기소개서 전문을 반환하는 기능
   * (AI 분석용 전체보기 용도)
   *
   * @param postId 모집글 ID
   * @return ApplyResumeResponse 리스트 (자기소개서 전문 포함)
   */
  public List<ApplyResumeResponse> getApplyResumesByPostId(Long postId) {
    List<Apply> applies = applyRepository.findWithUserAndResumeByPostId(postId);
    return applies.stream()
            .map(apply -> new ApplyResumeResponse(
                    apply.getUser().getId(),
                    apply.getUser().getProfile().getNickname(),
                    apply.getResume().getContent() // 자기소개서 전문
            ))
            .toList();
  }
}