package aibe.hosik.apply.service;

import aibe.hosik.analysis.entity.Analysis;
import aibe.hosik.analysis.repository.AnalysisRepository;
import aibe.hosik.analysis.service.AnalysisService;
import aibe.hosik.apply.dto.ApplyByResumeSkillResponse;
import aibe.hosik.apply.dto.ApplyDetailResponseDTO;
import aibe.hosik.apply.entity.Apply;
import aibe.hosik.apply.repository.ApplyRepository;
import aibe.hosik.post.entity.Post;
import aibe.hosik.post.repository.PostRepository;
import aibe.hosik.resume.entity.Resume;
//import aibe.hosik.resume.repository.ResumeRepository;
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
//  private final ResumeRepository resumeRepository; // Resume 테이블과 통신
  private final UserRepository userRepository; // User 테이블과 통신
  private final ResumeSkillRepository resumeSkillRepository;
  private final AnalysisRepository analysisRepository;
  private final AnalysisService analysisService;


  /**
   * 사용자가 특정 모집글에 특정 이력서를 가지고 지원하는 기능
   * @param userId 지원자 ID
   * @param postId 모집글 ID
   * @param resumeId 지원자가 선택한 이력서 ID
   */
  public void apply(Long userId, Long postId, Long resumeId, String reason) {
    Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found")); // postId로 모집글 조회

//    Resume resume = resumeRepository.findById(resumeId)
//            .orElseThrow(() -> new IllegalArgumentException("Resume not found")); // resumeId로 이력서 조회

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));


      // 한 번 더 검증 본인의 이력서인지 확인
//      if (!resume.getUser().getId().equals(userId)) {
//          throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 이력서만 사용할 수 있습니다.");
//      }
//todo: 원상복구 필요
    Apply apply = Apply.of(post, user, null, reason);
    applyRepository.save(apply); // DB에 저장

      log.info("AI 분석 시작 - applyId: {}", apply.getId());
      try {
          analysisService.analysisApply(apply.getId());

      } catch (Exception e) {
          log.error("AI 분석 중 오류 발생", e);
          // 지원 자체는 성공으로 처리하고 분석 오류만 로깅
      }
  }

    /**
     * 지원서 모아보기
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

        List<Apply> applies = applyRepository.findWithUserResumeAndAnalysisByPostId(postId);

        return applies.stream()
                .map(apply -> {
                    // 이력서에 연결된 스킬 정보 조회
                    List<String> skills = getSkillsByResumeId(apply.getResume().getId());
                    Analysis analysis = analysisRepository.findLatestByApplyId(apply.getId()).orElse(null);

                    // 정적 팩토리 메서드 활용
                    return ApplyByResumeSkillResponse.from(apply, skills, analysis);
                })
                .collect(Collectors.toList());
    }

    /**
     * 특정 모집글에 지원한 사람들의 자기소개서 전문을 반환하는 기능
     * 게시글 상세보기 기능
     */
    public ApplyDetailResponseDTO getApplyDetailByApplyId(Long applyId, User user) {
        Apply apply = applyRepository.findById(applyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "지원서를 찾을 수 없습니다."));

        if (!apply.getPost().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "모집글 작성자만 지원자 정보를 조회할 수 있습니다.");
        }

        List<String> skills = getSkillsByResumeId(apply.getResume().getId());
        Analysis analysis = analysisRepository.findLatestByApplyId(applyId).orElse(null);

        return ApplyDetailResponseDTO.from(apply, skills, analysis);
    }


    /**
     * 주어진 지원서 ID에 해당하는 지원서를 삭제합니다.
     *
     * @param applyId 삭제하려는 지원서의 식별자(ID)
     * @param user    현재 요청을 수행하는 사용자
     */
    public void deleteApply(Long applyId, User user) {
        Apply apply = applyRepository.findById(applyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "지원서를 찾을 수 없습니다."));

        if (!apply.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 지원서만 삭제할 수 있습니다.");
        }
        applyRepository.delete(apply);
    }

    public void updateIsSelected(Long applyId, boolean isselected, User user) {
        Apply apply = applyRepository.findById(applyId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "지원서를 찾을 수 없습니다"));

        if (!apply.getPost().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "모집글 작성자만 팀원을 선택할 수 있습니다.");
        }

        // 선택 업데이트
        apply.updateIsSelected(isselected);
        applyRepository.save(apply);
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