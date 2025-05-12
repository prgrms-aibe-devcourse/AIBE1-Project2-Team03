package aibe.hosik.profile;

import aibe.hosik.handler.exception.CustomException;
import aibe.hosik.handler.exception.ErrorCode;
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
//    private final ProfileStorageService profileStorageService;

  public ProfileResponse getProfileByUserId(Long userId) {
    Profile profile = profileRepository.findByUserId(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PROFILE));
    return ProfileResponse.from(profile);
  }

//  public Profile updateProfile(Long userId, String introduction, String imageUrl) {
//    log.info("Updating profile for user ID: {}", userId);
//    Profile profile = getProfileByUserId(userId);
//    profile.updateProfile(introduction, imageUrl);
//    return profileRepository.save(profile);
//  }
}