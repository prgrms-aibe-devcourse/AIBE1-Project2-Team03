package aibe.hosik.profile.dto;

/**
 * 프로필 업데이트 요청 DTO
 */
public record ProfileUpdateRequest(
        String introduction,
        String image
) {
    public static ProfileUpdateRequest of(String introduction, String image) {
        return new ProfileUpdateRequest(introduction, image);
    }
}