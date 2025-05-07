package aibe.hosik.profile;

import aibe.hosik.resume.Resume;
import aibe.hosik.resume.ResumeRepository;
import aibe.hosik.review.Review;
import aibe.hosik.review.ReviewRepository;
import aibe.hosik.user.User;
import aibe.hosik.user.UserRepository;
import aibe.hosik.apply.Apply;
import aibe.hosik.apply.ApplyRepository;
import aibe.hosik.post.Post;
import aibe.hosik.post.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final ReviewRepository reviewRepository;
    private final PostRepository postRepository;
    private final ApplyRepository applyRepository;

    // 파일 업로드 경로 설정
    private final String uploadDir = "src/main/resources/static/uploads/";

    /**
     * 사용자 ID로 프로필 조회 (기본)
     */
    @Transactional(readOnly = true)
    public Profile getProfileByUserId(Long userId) {
        log.info("Getting profile for user ID: {}", userId);
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 프로필이 존재하지 않습니다: " + userId));
    }

    /**
     * 사용자 ID로 프로필 조회 (대표 자기소개서 포함)
     */
    @Transactional(readOnly = true)
    public Profile getProfileWithMainResume(Long userId) {
        log.info("Getting profile with main resume for user ID: {}", userId);
        return profileRepository.findByUserIdWithMainResume(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 프로필이 존재하지 않습니다: " + userId));
    }

    /**
     * 프로필 ID로 프로필 조회 (리뷰 포함)
     */
    @Transactional(readOnly = true)
    public Profile getProfileWithReviews(Long profileId) {
        log.info("Getting profile with reviews for profile ID: {}", profileId);
        return profileRepository.findByIdWithReviews(profileId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다: " + profileId));
    }

    /**
     * 프로필 정보 업데이트 (모달에서 사용)
     */
    @Transactional
    public Profile updateProfile(Long userId, String introduction, String imageUrl) {
        log.info("Updating profile for user ID: {}", userId);
        Profile profile = getProfileByUserId(userId);

        profile.setIntroduction(introduction);
        if (imageUrl != null && !imageUrl.isBlank()) {
            profile.setImageUrl(imageUrl);
        }

        return profileRepository.save(profile);
    }

    /**
     * 프로필 이미지 업로드 및 업데이트
     */
    @Transactional
    public Profile updateProfileImage(Long userId, MultipartFile imageFile) throws IOException {
        log.info("Updating profile image for user ID: {}", userId);
        Profile profile = getProfileByUserId(userId);

        if (imageFile != null && !imageFile.isEmpty()) {
            // 기존 이미지가 있다면 삭제
            if (profile.getImageUrl() != null && !profile.getImageUrl().isEmpty()
                    && !profile.getImageUrl().contains("default")) {
                try {
                    Path oldFilePath = Paths.get(uploadDir + profile.getImageUrl().substring(profile.getImageUrl().lastIndexOf('/') + 1));
                    Files.deleteIfExists(oldFilePath);
                } catch (IOException e) {
                    log.error("Error deleting old profile image: {}", e.getMessage());
                }
            }

            // 새 이미지 저장
            String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            Path targetPath = Paths.get(uploadDir + fileName);
            Files.createDirectories(targetPath.getParent());
            Files.copy(imageFile.getInputStream(), targetPath);

            // 이미지 URL 업데이트
            String imageUrl = "/uploads/" + fileName;
            profile.setImageUrl(imageUrl);

            return profileRepository.save(profile);
        }

        return profile;
    }

    /**
     * 새 사용자 등록 시 기본 프로필 생성
     */
    @Transactional
    public Profile createProfile(User user) {
        log.info("Creating profile for user: {}", user.getEmail());
        Profile profile = new Profile();
        profile.setUser(user);
        profile.setImageUrl("/images/default-profile.png"); // 기본 프로필 이미지

        return profileRepository.save(profile);
    }

    /**
     * 프로필 소유자 확인
     */
    @Transactional(readOnly = true)
    public boolean isProfileOwner(Long userId, Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다: " + profileId));

        return profile.getUser().getId().equals(userId);
    }

    /**
     * 리뷰 삭제 (권한 체크 포함)
     */
    @Transactional
    public void deleteReview(Long reviewId, Long currentUserId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다: " + reviewId));

        // 삭제 권한 확인
        Profile targetProfile = review.getTargetUser().getProfile();
        if (!targetProfile.canDeleteReview(review, currentUserId)) {
            throw new IllegalArgumentException("리뷰를 삭제할 권한이 없습니다.");
        }

        reviewRepository.delete(review);
        log.info("Review deleted. ID: {}", reviewId);
    }

    /**
     * 최근 자기소개서 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Resume> getRecentResumes(Long userId, int limit) {
        log.info("Getting recent resumes for user ID: {}", userId);
        return resumeRepository.findByUserIdOrderByUpdatedAtDesc(userId, Pageable.ofSize(limit));
    }

    /**
     * 지원한 모집글 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<Apply> getAppliedPosts(Long userId, Pageable pageable) {
        log.info("Getting applied posts for user ID: {}", userId);
        return applyRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 작성한 모집글 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<Post> getCreatedPosts(Long userId, Pageable pageable) {
        log.info("Getting created posts for user ID: {}", userId);
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
}