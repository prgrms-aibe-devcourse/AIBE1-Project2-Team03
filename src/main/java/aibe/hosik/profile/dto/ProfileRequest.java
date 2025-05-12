package aibe.hosik.profile.dto;

import org.springframework.web.multipart.MultipartFile;

public record ProfileRequest(
    String introduction,
    String nickname
) {
}
