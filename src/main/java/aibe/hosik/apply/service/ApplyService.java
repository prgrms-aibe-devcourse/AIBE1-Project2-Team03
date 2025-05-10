package aibe.hosik.apply.service;

import aibe.hosik.apply.dto.ApplyByResumeSkillResponse;
import aibe.hosik.apply.dto.ApplyResumeResponse;
import aibe.hosik.apply.dto.ApplyUserResponse;
import aibe.hosik.apply.entity.Apply;
import aibe.hosik.apply.repository.ApplyRepository;
import aibe.hosik.post.entity.Post;
import aibe.hosik.post.repository.PostRepository;
import aibe.hosik.resume.entity.Resume;
import aibe.hosik.resume.repository.ResumeRepository;
import aibe.hosik.skill.entity.ResumeSkill;
import aibe.hosik.skill.repository.ResumeSkillRepository;
import aibe.hosik.user.User;
import aibe.hosik.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplyService {

  private final ApplyRepository applyRepository; // Apply 테이블과 통신하는 레포
  private final PostRepository postRepository; // Post 테이블과 통신
  private final ResumeRepository resumeRepository; // Resume 테이블과 통신
  private final UserRepository userRepository; // User 테이블과 통신
  private final ResumeSkillRepository resumeSkillRepository;


  /**
   * 사용자가 특정 모집글에 특정 이력서를 가지고 지원하는 기능
   * @param userId 지원자 ID
   * @param postId 모집글 ID
   * @param resumeId 지원자가 선택한 이력서 ID
   */
  public void apply(Long userId, Long postId, Long resumeId, String reason) {
    Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found")); // postId로 모집글 조회

    Resume resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new IllegalArgumentException("Resume not found")); // resumeId로 이력서 조회

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

      // 한 번 더 검증 본인의 이력서인지 확인
      if (!resume.getUser().getId().equals(userId)) {
          throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 이력서만 사용할 수 있습니다.");
      }

    Apply apply = Apply.of(post, user, resume, reason);

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
   */
  public List<ApplyResumeResponse> getApplyResumesByPostId(Long postId) {
    List<Apply> applies = applyRepository.findWithUserAndResumeByPostId(postId);
    return applies.stream()
            .map(apply -> new ApplyResumeResponse(
                    apply.getUser().getId(),
                    apply.getResume().getId(),
                    apply.getResume().getContent(),
                    apply.getResume().getPersonality()
            ))
            .toList();
  }


    /**
     * 지정된 구인 공고 ID에 연결된 지원 데이터를 기반으로, 지원 정보와 이력서에 포함된 스킬 정보를 함께 반환합니다.
     *
     * @param postId 대상 구인 공고 ID
     * @return ApplyByResumeSkillResponse 객체의 리스트. 각 객체는 지원 정보 및 해당 지원자의 이력서에 포함된 스킬 정보를 포함합니다.
     */
    public List<ApplyByResumeSkillResponse> getApplyResumeWithSkillsByPostId(Long postId, User user) {
        // 모집글 정보 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "모집글을 찾을 수 없습니다."));

        // 모집글 작성자 검증
        if (!post.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "모집글 작성자만 지원자 정보를 조회할 수 있습니다.");
        }

        List<Apply> applies = applyRepository.findWithUserResumeAndSkillsByPostId(postId);

        return applies.stream()
                .map(apply -> {
                    // 이력서에 연결된 스킬 정보 조회
                    List<String> skills = getSkillsByResumeId(apply.getResume().getId());

                    // 정적 팩토리 메서드 활용
                    return ApplyByResumeSkillResponse.from(apply, skills);
                })
                .collect(Collectors.toList());
    }

  /**
   * 이력서 ID로 해당 이력서에 연결된 모든 스킬 이름을 조회하는 내부 메서드
   *
   * @param resumeId 이력서 ID
   * @return 스킬 이름 목록
   */
  private List<String> getSkillsByResumeId(Long resumeId) {
    List<ResumeSkill> resumeSkills = resumeSkillRepository.findByResumeId(resumeId);
    return resumeSkills.stream()
            .map(rs -> rs.getSkill().getName())
            .collect(Collectors.toList());
  }
}