package aibe.hosik.post.dto;

import org.springframework.web.multipart.MultipartFile;

public record ImageUploadDTO(MultipartFile image) {
}
