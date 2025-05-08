package aibe.hosik.analysis;

import aibe.hosik.apply.entity.Apply;
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
public class Analysis {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String result;

  @Column(nullable = false)
  private String summary;

  @Column(nullable = false)
  private int score;

  @ManyToOne(fetch = FetchType.LAZY)
  private Apply apply;
}
