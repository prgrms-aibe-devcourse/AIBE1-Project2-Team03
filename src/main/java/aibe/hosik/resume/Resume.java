package aibe.hosik.resume;

import aibe.hosik.common.TimeEntity;
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
}
