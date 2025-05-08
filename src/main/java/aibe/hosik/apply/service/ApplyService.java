package aibe.hosik.apply.service;

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
//            .userId(userId) // 누가
            .user(user)
            .resume(resume) // 어떤 이력서로
            .isSelected(false) // 기본값은 미선정
            .build();

    applyRepository.save(apply); // DB에 저장
  }
}