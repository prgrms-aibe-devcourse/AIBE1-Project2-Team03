package aibe.hosik.apply.entity;

import aibe.hosik.common.TimeEntity;
import aibe.hosik.post.entity.Post;
import aibe.hosik.resume.Resume;
import aibe.hosik.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Apply extends TimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column
  private boolean isSelected;

  @Column
  private String reason;

  @ManyToOne(fetch = FetchType.LAZY)
  private Post post;

  @ManyToOne(fetch = FetchType.LAZY)
  private Resume resume;

  @ManyToOne(fetch = FetchType.LAZY)
  private User user;
}
