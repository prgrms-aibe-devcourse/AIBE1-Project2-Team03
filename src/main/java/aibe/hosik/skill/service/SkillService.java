package aibe.hosik.skill.service;

import aibe.hosik.post.entity.Post;
import aibe.hosik.skill.entity.PostSkill;
import aibe.hosik.skill.entity.Skill;
import aibe.hosik.skill.repository.PostSkillRepository;
import aibe.hosik.skill.repository.SkillRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillService {
//  private final SkillRepository skillRepository;
//  private final PostSkillRepository postSkillRepository;
//
//  @Transactional
//  public List<String> saveSkills(Post post, List<String> skills) {
//    List<String> savedSkills = new ArrayList<>();
//    for(String s : skills) {
//      Skill skill = skillRepository.findByName(s)
//              .orElseGet(() -> skillRepository.save(Skill.builder().name(s).build()));
//      postSkillRepository.save(PostSkill.builder().post(post).skill(skill).build());
//      savedSkills.add(skill.getName());
//    }
//    return savedSkills;
//  }
//
//  @Transactional
//  public void updateSkills(Post post, List<String> skills) {
//    postSkillRepository.deleteByPostId(post.getId());
//    saveSkills(post, skills);
//  }
//
//  public List<String> getSkills(Post post) {
//    return post.getPostSkills().stream()
//            .map(s -> s.getSkill().getName())
//            .collect(Collectors.toList());
//  }
}
