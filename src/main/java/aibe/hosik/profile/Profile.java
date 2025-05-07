package aibe.hosik.profile;

import aibe.hosik.user.User;
import aibe.hosik.resume.Resume;
import aibe.hosik.review.Review;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "profile")
@Getter @Setter
@NoArgsConstructor
public class Profile {

  @Id
  private Long id;

  @OneToOne
  @MapsId
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "introduction")
  private String introduction;

  @Column(name = "image_url")
  private String imageUrl;

  // 대표 자기소개서 관계 추가
  // Resume 엔티티에서 is_main 필드로 대표 자기소개서 판단
  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private List<Resume> resumes = new ArrayList<>();

  // 리뷰 관리를 위한 관계 추가
  @OneToMany(mappedBy = "targetUser", fetch = FetchType.LAZY)
  private List<Review> receivedReviews = new ArrayList<>();

  // 프로필 조회 시 대표 자기소개서 가져오는 편의 메서드
  public Resume getMainResume() {
    if (resumes == null || resumes.isEmpty()) {
      return null;
    }

    // is_main = true인 자기소개서 찾기
    return resumes.stream()
            .filter(Resume::isMain)
            .findFirst()
            .orElse(null);
  }

  // 리뷰 삭제 권한 확인 편의 메서드
  public boolean canDeleteReview(Review review, Long currentUserId) {
    // 1. 자신의 프로필이면 모든 리뷰 삭제 가능
    if (user.getId().equals(currentUserId)) {
      return true;
    }

    // 2. 타인의 프로필에서는 자신이 작성한 리뷰만 삭제 가능
    return review.getAuthor().getId().equals(currentUserId);
  }
}
