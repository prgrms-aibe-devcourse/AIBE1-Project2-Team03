package aibe.hosik.profile;

import aibe.hosik.user.User;
import aibe.hosik.resume.Resume;
import aibe.hosik.review.Review;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "introduction")
    private String introduction;

    @Column(name = "image")
    private String image;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "is_public")
    private boolean isPublic = false;

    // 대표 자기소개서 관계
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Resume> resumes = new ArrayList<>();

    // 리뷰 관계 - 피어리뷰 포함 모든 리뷰를 저장
    @OneToMany(mappedBy = "reviewee", fetch = FetchType.LAZY)
    private List<Review> receivedReviews = new ArrayList<>();

    // 생성자
    @Builder(builderMethodName = "baseBuilder")
    private Profile(User user, String introduction, String image, String nickname, boolean isPublic) {
        this.user = user;
        this.introduction = introduction;
        this.image = image;
        this.nickname = nickname;
        this.isPublic = isPublic;
    }

    // 정적 팩토리 메소드 - 기본 프로필 생성
    public static Profile createBasicProfile(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return Profile.baseBuilder()
                .user(user)
                .image("/images/default-profile.png")
                .isPublic(false)
                .build();
    }

    // 정적 팩토리 메소드 - 완전한 프로필 생성
    public static Profile createComplete(User user, String introduction, String image, String nickname, boolean isPublic) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return Profile.baseBuilder()
                .user(user)
                .introduction(introduction)
                .image(image)
                .nickname(nickname)
                .isPublic(isPublic)
                .build();
    }

    // 상태 변경 메소드 (불변성을 유지하지 않음 - JPA 엔티티 특성상)
    public Profile updateIntroduction(String introduction) {
        this.introduction = introduction;
        return this;
    }

    public Profile updateImage(String image) {
        if (image != null && !image.isBlank()) {
            this.image = image;
        }
        return this;
    }

    public Profile updateNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public Profile makePublic() {
        this.isPublic = true;
        return this;
    }

    public Profile makePrivate() {
        this.isPublic = false;
        return this;
    }

    public Profile updateProfile(String introduction, String image) {
        return this
                .updateIntroduction(introduction)
                .updateImage(image);
    }

    public Profile updateProfile(String introduction, String image, String nickname) {
        return this
                .updateIntroduction(introduction)
                .updateImage(image)
                .updateNickname(nickname);
    }

    // 비즈니스 로직 메소드
    public Resume getMainResume() {
        if (resumes == null || resumes.isEmpty()) {
            return null;
        }

        return resumes.stream()
                .filter(Resume::isMain)
                .findFirst()
                .orElse(null);
    }

    public boolean canBeViewedBy(User viewer) {
        // 공개 프로필이거나 자신의 프로필이면 볼 수 있음
        return isPublic || user.equals(viewer);
    }

    // 컬렉션에 대한 방어적 복사 제공
    public List<Resume> getResumes() {
        return new ArrayList<>(resumes);
    }

    public List<Review> getReceivedReviews() {
        return new ArrayList<>(receivedReviews);
    }
}