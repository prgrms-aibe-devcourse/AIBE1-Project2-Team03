package aibe.hosik.post.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String upload(MultipartFile image) throws Exception;
}
