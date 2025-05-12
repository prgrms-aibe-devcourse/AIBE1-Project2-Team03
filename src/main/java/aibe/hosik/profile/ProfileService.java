package aibe.hosik.profile;

import aibe.hosik.common.exception.AccessDeniedException;
import aibe.hosik.common.exception.ResourceNotFoundException;
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

import java.util.List;

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
    private final ProfileStorageService profileStorageService; // 프로필 전용 스토리지 서비스 주입

    /**
     * 사용자 ID로 프로필 조회 (기본)
     */
    @Transactional(readOnly = true)
    public Profile getProfileByUserId(Long userId) {
        log.info("Getting profile for user ID: {}", userId);
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 사용자의 프로필이 존재하지 않습니다: " + userId));
    }

    /**
     * 사용자 ID로 프로필 조회 (대표 자기소개서 포함)
     */
    @Transactional(readOnly = true)
    public Profile getProfileWithMainResume(Long userId) {
        log.info("Getting profile with main resume for user ID: {}", userId);
        return profileRepository.findByUserIdWithMainResume(userId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 사용자의 프로필이 존재하지 않습니다: " + userId));
    }

    /**
     * 프로필 ID로 프로필 조회 (리뷰 포함)
     */
    @Transactional(readOnly = true)
    public Profile getProfileWithReviews(Long profileId) {
        log.info("Getting profile with reviews for profile ID: {}", profileId);
        return profileRepository.findByIdWithReviews(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("프로필을 찾을 수 없습니다: " + profileId));
    }

    /**
     * 프로필 정보 업데이트
     */
    @Transactional
    public Profile updateProfile(Long userId, String introduction, String imageUrl) {
        log.info("Updating profile for user ID: {}", userId);
        Profile profile = getProfileByUserId(userId);
        profile.updateProfile(introduction, imageUrl);
        return profileRepository.save(profile);
    }

    /**
     * 프로필 공개 여부 설정
     */
    @Transactional
    public Profile updateProfileVisibility(Long userId, boolean isPublic) {
        log.info("Updating profile visibility for user ID: {}", userId);
        Profile profile = getProfileByUserId(userId);
        if (isPublic) {
            profile.makePublic();
        } else {
            profile.makePrivate();
        }
        return profileRepository.save(profile);
    }

    /**
     * 프로필 닉네임 업데이트
     */
    @Transactional
    public Profile updateProfileNickname(Long userId, String nickname) {
        log.info("Updating profile nickname for user ID: {}", userId);
        Profile profile = getProfileByUserId(userId);
        profile.updateNickname(nickname);
        return profileRepository.save(profile);
    }

    /**
     * 프로필 이미지 업로드 및 업데이트 (Supabase 사용)
     */
    @Transactional
    public Profile updateProfileImage(Long userId, MultipartFile imageFile) throws Exception {
        log.info("Updating profile image for user ID: {}", userId);
        Profile profile = getProfileByUserId(userId);

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // ProfileStorageService를 통한 이미지 업로드
                String imageUrl = profileStorageService.uploadProfileImage(imageFile);
                log.info("Profile image uploaded to Supabase: {}", imageUrl);

                // 프로필 이미지 URL 업데이트
                profile.updateImage(imageUrl);
                return profileRepository.save(profile);
            } catch (Exception e) {
                log.error("Failed to upload profile image: {}", e.getMessage());
                throw new Exception("프로필 이미지 업로드에 실패했습니다: " + e.getMessage());
            }
        }

        return profile;
    }

    /**
     * 새 사용자 등록 시 기본 프로필 생성
     */
    @Transactional
    public Profile createProfile(User user) {
        log.info("Creating profile for user: {}", user.getEmail());
        Profile profile = Profile.createBasicProfile(user);
        return profileRepository.save(profile);
    }

    /**
     * 프로필 소유자 확인
     */
    @Transactional(readOnly = true)
    public boolean isProfileOwner(Long userId, Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("프로필을 찾을 수 없습니다: " + profileId));

        return profile.getUser().getId().equals(userId);
    }

    /**
     * 리뷰 삭제 (권한 체크 포함)
     */
    @Transactional
    public void deleteReview(Long reviewId, Long currentUserId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("리뷰를 찾을 수 없습니다: " + reviewId));

        // 삭제 권한 확인
        Profile targetProfile = review.getReviewee().getProfile();
        if (!targetProfile.canDeleteReview(review, currentUserId)) {
            throw new AccessDeniedException("리뷰를 삭제할 권한이 없습니다.");
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

    /**
     * 공개 프로필 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Profile> getAllPublicProfiles() {
        log.info("Getting all public profiles");
        return profileRepository.findAllPublicProfiles();
    }

    /**
     * 공개 프로필 검색
     */
    @Transactional(readOnly = true)
    public List<Profile> searchPublicProfiles(String keyword) {
        log.info("Searching public profiles with keyword: {}", keyword);
        return profileRepository.searchPublicProfilesByNickname(keyword);
    }

    /**
     * 프로필 포트폴리오 조회
     */
    @Transactional(readOnly = true)
    public List<Resume> getPortfolios(Long userId, User currentUser) {
        log.info("Getting portfolios for user ID: {}", userId);

        Profile profile = getProfileByUserId(userId);

        // 프로필이 비공개인 경우 현재 사용자가 볼 수 있는지 확인
        if (!profile.isPublic() && !profile.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("비공개 프로필의 포트폴리오에 접근할 수 없습니다.");
        }

        return resumeRepository.findPortfoliosByUserId(userId);
    }
}