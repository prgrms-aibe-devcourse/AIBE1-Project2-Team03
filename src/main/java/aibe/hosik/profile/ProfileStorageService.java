//package aibe.hosik.profile;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class ProfileStorageService {
//
//    @Value("${supabase.url}")
//    private String url;
//
//    @Value("${supabase.access-key}")
//    private String accessKey;
//
//    @Value("${supabase.buckets.profile:profile-image}")
//    private String bucketName;
//
//    /**
//     * 프로필 이미지 업로드
//     */
//    public String uploadProfileImage(MultipartFile file) throws Exception {
//        // 파일 타입 검증
//        String contentType = file.getContentType();
//        if (contentType == null || !contentType.startsWith("image/")) {
//            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
//        }
//
//        String uuid = UUID.randomUUID().toString();
//        String extension = Optional.ofNullable(file.getContentType())
//                .map(ct -> ct.split("/")[1])
//                .orElse("jpg");
//
//        String boundary = "Boundary-%s".formatted(uuid);
//        String filename = "%s.%s".formatted(uuid, extension);
//
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("%s/storage/v1/object/%s/%s".formatted(url, bucketName, filename)))
//                .header("Authorization", "Bearer " + accessKey)
//                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
//                .POST(ofMimeMultipartData(file, boundary))
//                .build();
//
//        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
//
//        if (response.statusCode() != 200) {
//            throw new IOException("Supabase upload error: " + response.body());
//        }
//
//        log.info("Profile image uploaded to Supabase bucket '{}': {}", bucketName, filename);
//        return "%s/storage/v1/object/public/%s/%s".formatted(url, bucketName, filename);
//    }
//
//    /**
//     * 멀티파트 폼 데이터 생성
//     */
//    private HttpRequest.BodyPublisher ofMimeMultipartData(MultipartFile file, String boundary) throws IOException {
//        return HttpRequest.BodyPublishers.ofByteArrays(List.of(
//                ("--" + boundary + "\r\n" +
//                        "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getOriginalFilename() + "\"\r\n" +
//                        "Content-Type: " + file.getContentType() + "\r\n\r\n").getBytes(),
//                file.getBytes(),
//                ("\r\n--" + boundary + "--\r\n").getBytes()
//        ));
//    }
//}