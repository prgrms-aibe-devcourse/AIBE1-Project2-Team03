package aibe.hosik.resume;

import aibe.hosik.common.TimeEntity;
import aibe.hosik.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "resume")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Resume extends TimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String content;

  @Column(nullable = false)
  private boolean isMain;

  @Column
  private String personality;

  @Column
  private String portfolio;

  @ManyToOne(fetch = FetchType.LAZY)
  private User user;

  @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ResumeSkill> skills = new HashSet<>();

  // 생성자
  @Builder(builderMethodName = "baseBuilder")
  private Resume(User user, String title, String content, String personality, String portfolio, boolean isMain) {
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null");
    }
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("Title cannot be empty");
    }
    if (content == null || content.isBlank()) {
      throw new IllegalArgumentException("Content cannot be empty");
    }

    this.user = user;
    this.title = title;
    this.content = content;
    this.personality = personality;
    this.portfolio = portfolio;
    this.isMain = isMain;
  }

  // 정적 팩토리 메서드 - 기본 자기소개서 생성
  public static Resume createBasic(User user, String title, String content) {
    return Resume.baseBuilder()
            .user(user)
            .title(title)
            .content(content)
            .isMain(false)
            .build();
  }

  // 정적 팩토리 메서드 - 완전한 자기소개서 생성
  public static Resume createComplete(User user, String title, String content,
                                      String personality, String portfolio, boolean isMain) {
    return Resume.baseBuilder()
            .user(user)
            .title(title)
            .content(content)
            .personality(personality)
            .portfolio(portfolio)
            .isMain(isMain)
            .build();
  }

  // 상태 변경 메서드
  public Resume updateTitle(String title) {
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("Title cannot be empty");
    }
    this.title = title;
    return this;
  }

  public Resume updateContent(String content) {
    if (content == null || content.isBlank()) {
      throw new IllegalArgumentException("Content cannot be empty");
    }
    this.content = content;
    return this;
  }

  public Resume updatePersonality(String personality) {
    this.personality = personality;
    return this;
  }

  public Resume updatePortfolio(String portfolio) {
    this.portfolio = portfolio;
    return this;
  }

  public Resume setAsMain() {
    this.isMain = true;
    return this;
  }

  public Resume unsetMain() {
    this.isMain = false;
    return this;
  }

  public Resume updateResume(String title, String content) {
    return this
            .updateTitle(title)
            .updateContent(content);
  }

  public Resume updateResume(String title, String content, String personality, String portfolio) {
    return this
            .updateTitle(title)
            .updateContent(content)
            .updatePersonality(personality)
            .updatePortfolio(portfolio);
  }

  // 비즈니스 로직 메서드
  public boolean hasPortfolio() {
    return portfolio != null && !portfolio.isBlank();
  }

  public void addSkill(ResumeSkill skill) {
    this.skills.add(skill);
    skill.setResume(this);
  }

  public void removeSkill(ResumeSkill skill) {
    this.skills.remove(skill);
    skill.setResume(null);
  }

  // 컬렉션에 대한 방어적 복사 제공
  public Set<ResumeSkill> getSkills() {
    return new HashSet<>(skills);
  }
}