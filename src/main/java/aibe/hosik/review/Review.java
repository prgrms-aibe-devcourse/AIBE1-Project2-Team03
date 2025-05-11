package aibe.hosik.review;

import aibe.hosik.apply.entity.Apply;
import aibe.hosik.common.TimeEntity;
import aibe.hosik.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends TimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reviewer_id", nullable = false)
  private User reviewer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reviewee_id", nullable = false)
  private User reviewee;

  @Column(nullable = false, length = 1000)
  private String content;

  @Column
  private Integer rating; // 1-5 평점 (선택사항)

  // 리뷰 유형 구분
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReviewType reviewType;

  // 피어리뷰인 경우 프로젝트 참조
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "apply_id")
  private Apply apply;

  // 생성자
  @Builder(builderMethodName = "baseBuilder")
  private Review(User reviewer, User reviewee, String content, Integer rating, ReviewType reviewType, Apply apply) {
    this.reviewer = reviewer;
    this.reviewee = reviewee;
    this.content = content;
    this.rating = rating;
    this.reviewType = reviewType;
    this.apply = apply;
  }

  // 비즈니스 로직 메서드
  public boolean canBeModifiedBy(User user) {
    return reviewer.getId().equals(user.getId());
  }

  public boolean canBeViewedBy(User user) {
    // 프로필 리뷰는 리뷰이의 프로필 공개 여부에 따라 결정
    if (reviewType == ReviewType.PROFILE_REVIEW) {
      return reviewee.getProfile().canBeViewedBy(user) ||
              user.getId().equals(reviewer.getId());
    }

    // 피어리뷰는 프로젝트 참여자와 모집글 작성자만 조회 가능
    if (reviewType == ReviewType.PEER_REVIEW && apply != null) {
      return user.getId().equals(reviewer.getId()) ||
              user.getId().equals(reviewee.getId()) ||
              user.getId().equals(apply.getPost().getUser().getId());
    }

    return false;
  }

  public boolean canBeDeletedBy(User user) {
    // 프로필 리뷰: 리뷰어 또는 프로필 소유자
    if (reviewType == ReviewType.PROFILE_REVIEW) {
      return user.getId().equals(reviewer.getId()) ||
              user.getId().equals(reviewee.getId());
    }

    // 피어리뷰: 리뷰어만
    if (reviewType == ReviewType.PEER_REVIEW) {
      return user.getId().equals(reviewer.getId());
    }

    return false;
  }

  // 정적 팩토리 메서드
  public static Review createProfileReview(User reviewer, User reviewee, String content) {
    return Review.baseBuilder()
            .reviewer(reviewer)
            .reviewee(reviewee)
            .content(content)
            .reviewType(ReviewType.PROFILE_REVIEW)
            .build();
  }

  public static Review createPeerReview(User reviewer, User reviewee, Apply apply, String content, Integer rating) {
    if (!apply.isSelected()) {
      throw new IllegalArgumentException("매칭 성공한 프로젝트에 대해서만 피어리뷰를 작성할 수 있습니다.");
    }

    return Review.baseBuilder()
            .reviewer(reviewer)
            .reviewee(reviewee)
            .apply(apply)
            .content(content)
            .rating(rating)
            .reviewType(ReviewType.PEER_REVIEW)
            .build();
  }

  // 리뷰 유형 enum
  public enum ReviewType {
    PROFILE_REVIEW, // 기존 프로필 리뷰
    PEER_REVIEW     // 프로젝트 완료 후 피어리뷰
  }
}