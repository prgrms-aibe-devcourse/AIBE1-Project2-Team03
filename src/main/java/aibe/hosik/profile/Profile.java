package aibe.hosik.profile;

import aibe.hosik.user.User;
import aibe.hosik.resume.Resume;
import aibe.hosik.review.Review;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "profile")
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

    // 리뷰 관계
    @OneToMany(mappedBy = "reviewee", fetch = FetchType.LAZY)
    private List<Review> receivedReviews = new ArrayList<>();

    // 생성자 (JPA용)
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
        return new Profile(user, null, "/images/default-profile.png", null, false);
    }

    // 정적 팩토리 메소드 - 완전한 프로필 생성
    public static Profile createComplete(User user, String introduction, String image, String nickname, boolean isPublic) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return new Profile(user, introduction, image, nickname, isPublic);
    }

    // 빌더 패턴 접근
    public static ProfileBuilder builder(User user) {
        return new ProfileBuilder(user);
    }

    // 접근자 메소드
    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getIntroduction() { return introduction; }
    public String getImage() { return image; }
    public String getNickname() { return nickname; }
    public boolean isPublic() { return isPublic; }
    public List<Resume> getResumes() { return new ArrayList<>(resumes); }
    public List<Review> getReceivedReviews() { return new ArrayList<>(receivedReviews); }

    // 상태 변경 메소드
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

    public boolean canDeleteReview(Review review, Long currentUserId) {
        // 자신의 프로필이면 모든 리뷰 삭제 가능
        if (user.getId().equals(currentUserId)) {
            return true;
        }

        // 타인의 프로필에서는 자신이 작성한 리뷰만 삭제 가능
        return review.getReviewer().getId().equals(currentUserId);
    }

    public boolean canBeViewedBy(User viewer) {
        // 공개 프로필이거나 자신의 프로필이면 볼 수 있음
        return isPublic || user.equals(viewer);
    }

    // 내부 빌더 클래스
    public static class ProfileBuilder {
        private final User user;
        private String introduction;
        private String image = "/images/default-profile.png"; // 기본값
        private String nickname;
        private boolean isPublic = false;

        private ProfileBuilder(User user) {
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }
            this.user = user;
        }

        public ProfileBuilder introduction(String introduction) {
            this.introduction = introduction;
            return this;
        }

        public ProfileBuilder image(String image) {
            this.image = image;
            return this;
        }

        public ProfileBuilder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public ProfileBuilder isPublic(boolean isPublic) {
            this.isPublic = isPublic;
            return this;
        }

        public Profile build() {
            return new Profile(user, introduction, image, nickname, isPublic);
        }
    }
}