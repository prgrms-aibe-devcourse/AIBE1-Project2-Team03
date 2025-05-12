package aibe.hosik.profile;

import aibe.hosik.handler.exception.CustomException;
import aibe.hosik.handler.exception.ErrorCode;
import aibe.hosik.post.service.StorageService;
import aibe.hosik.profile.dto.ProfileRequest;
import aibe.hosik.profile.dto.ProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {
  private final ProfileRepository profileRepository;
  private final StorageService storageService;

  public ProfileResponse getProfileByUserId(Long userId) {
    Profile profile = profileRepository.findByUserId(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PROFILE));
    return ProfileResponse.from(profile);
  }

  public void updateProfile(ProfileRequest request, Long userId) {
    Profile profile = profileRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PROFILE));

    String profileImage = storageService.upload(request.image());

    Profile updated = profile.toBuilder()
        .nickname(request.nickname())
        .introduction(request.introduction())
        .image(profileImage)
        .build();

    profileRepository.save(updated);
  }
}