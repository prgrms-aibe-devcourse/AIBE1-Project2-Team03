package aibe.hosik.post.service;

import aibe.hosik.post.dto.*;
import aibe.hosik.post.entity.Post;
import aibe.hosik.post.repository.PostRepository;
import aibe.hosik.skill.repository.PostSkillRepository;
import aibe.hosik.skill.repository.SkillRepository;
import aibe.hosik.skill.entity.PostSkill;
import aibe.hosik.skill.entity.Skill;
import aibe.hosik.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
  private final PostRepository postRepository;
  private final SkillRepository skillRepository;
  private final PostSkillRepository postSkillRepository;

  /**
   * 모든 게시글을 조회하는 메서드입니다.
   *
   * @return 게시글 정보와 관련된 스킬 정보 및 현재 참여자 수를 포함한 PostResponseDTO 객체 리스트
   */
  @Override
  public List<PostResponseDTO> getAllPosts() {
    // findAllWithSkills로 한 번에 fetch(post, postSkills, skill)
    List<Post> posts = postRepository.findAllWithSkills();

    return posts.stream()
            .map(post -> {
              // fetch된 postSkills에서 skill 추출
              List<String> skills = post.getPostSkills().stream()
                      .map(s -> s.getSkill().getName())
                      .collect(Collectors.toList());

              // TODO : 현재 참여자 수 계산
              Integer currentCount = 0;

              // DTO 정적 팩토리 메서드 활용
              return PostResponseDTO.from(post, skills, currentCount);
            }).collect(Collectors.toList());
  }

  /**
   * 게시글을 생성하고 저장하는 메서드입니다.
   *
   * @param dto  게시글 생성에 필요한 정보를 담고 있는 PostRequestDTO 객체
   * @param user 게시글 생성 요청을 보낸 사용자 객체
   * @return 저장된 게시글 객체
   */
  @Override
  @Transactional
  public PostResponseDTO createPost(PostRequestDTO dto, User user) {
    // toEntity 사용해서 Post 객체 생성
    Post post = dto.toEntity(user);
    // 생성한 객체 Post 저장
    Post savePost = postRepository.save(post);
    
    // 연관된 skill 정보 저장
    List<String> skills = new ArrayList<>();
    // Skill 찾거나 생성
    for(String skillName : dto.skills()) {
      Skill skill = skillRepository.findByName(skillName)
              .orElseGet(() -> skillRepository.save(Skill.builder().name(skillName).build()));

      PostSkill postSkill = PostSkill.builder()
              .post(savePost)
              .skill(skill)
              .build();

      // post-skill 연관관계 추가
      postSkillRepository.save(postSkill);
      // 응답 DTO 스킬 저장
      skills.add(skill.getName());
    }
    // TODO: 참여자 수 계산 로직 필요
    int currentCount = 0;
    return PostResponseDTO.from(savePost, skills, currentCount);
  }

  /**
   * 특정 게시글의 상세 정보를 조회하는 메서드입니다.
   *
   * @param postId 상세 정보를 조회하려는 게시글의 ID
   * @return 게시글 정보, 관련된 스킬 정보, 매칭 사용자 정보를 포함한 PostDetailDTO 객체
   */
  @Override
  public PostDetailDTO getPostDetail(Long postId) {
    // 게시글 정보 조회
    Post post = postRepository.findByIdWithSkills(postId)
            .orElseThrow();

    // 스킬 이름 조회
    List<String> skills = post.getPostSkills().stream()
            .map(s -> s.getSkill().getName())
            .collect(Collectors.toList());

    // 매칭 사용자 정보 조회
    // TODO : 실제 매칭된 사용자 조회
    List<MatchedUserDTO> matchedUsers = new ArrayList<>();

    return PostDetailDTO.from(post,skills, matchedUsers);
  }

  /**
   * 특정 게시글을 삭제하는 메서드입니다.
   *
   * @param postId 삭제하려는 게시글의 ID
   * @throws ResponseStatusException 작성자가 아닐 경우, HTTP 상태 코드 FORBIDDEN과 함께 예외 발생
   */
  @Override
  @Transactional
  public void deletePost(Long postId, User user) {
    Post post = postRepository.findById(postId)
            .orElseThrow();
    if(post.getUser() == null || !post.getUser().getId().equals(user.getId())){
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자만 삭제할 수 있습니다");
    }
    postRepository.delete(post);
  }

  @Override
  @Transactional
  public PostResponseDTO updatePost(Long postId, PostPatchDTO dto, User user) {
    Post post = postRepository.findById(postId)
            .orElseThrow();
    if(!post.getUser().getId().equals(user.getId())){
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자만 삭제할 수 있습니다");
    }

    // 엔티티 메서드 이용해서 수정
    post.updatePatch(dto);

    List<String> skills = new ArrayList<>();
    if(dto.skills() != null){
      postSkillRepository.deleteByPostId(postId);

      for(String skillName : dto.skills()) {
        Skill skill = skillRepository.findByName(skillName)
                .orElseGet(() -> skillRepository.save(Skill.builder().name(skillName).build()));

        PostSkill postSkill = PostSkill.builder()
                .post(post)
                .skill(skill)
                .build();

        // post-skill 연관관계 추가
        postSkillRepository.save(postSkill);
        skills.add(skill.getName());
      }
    } else {
      skills = post.getPostSkills().stream()
              .map(s -> s.getSkill().getName())
              .collect(Collectors.toList());
    }

    // TODO: 참여자 수 계산 로직 필요
    int currentCount = 0;
    return PostResponseDTO.from(post, skills, currentCount);
  }
}
