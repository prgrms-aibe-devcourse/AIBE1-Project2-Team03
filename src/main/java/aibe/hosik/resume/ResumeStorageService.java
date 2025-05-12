//package aibe.hosik.resume;
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
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class ResumeStorageService {
//
//    @Value("${supabase.url}")
//    private String url;
//
//    @Value("${supabase.access-key}")
//    private String accessKey;
//
//    @Value("${supabase.buckets.resume:resume-files}")
//    private String bucketName;
//
//    /**
//     * 포트폴리오 파일 업로드
//     */
//    public String uploadPortfolioFile(MultipartFile file) throws Exception {
//        // 파일 타입 검증
//        String contentType = file.getContentType();
//        if (contentType == null) {
//            throw new IllegalArgumentException("파일 타입을 확인할 수 없습니다.");
//        }
//
//        // 허용되는 파일 타입 목록
//        String[] allowedTypes = {"application/pdf", "application/msword",
//                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
//                "application/vnd.ms-powerpoint",
//                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
//                "text/plain", "application/zip"};
//
//        boolean isAllowed = Arrays.stream(allowedTypes).anyMatch(contentType::equals);
//        if (!isAllowed) {
//            throw new IllegalArgumentException("지원되지 않는 파일 형식입니다. PDF, Word, PowerPoint, 텍스트, ZIP 파일만 업로드 가능합니다.");
//        }
//
//        String uuid = UUID.randomUUID().toString();
//        String extension = Optional.ofNullable(file.getContentType())
//                .map(ct -> {
//                    String[] parts = ct.split("/");
//                    if (parts.length > 1) {
//                        return parts[1];
//                    } else {
//                        // application/octet-stream 등의 경우 확장자 추출
//                        String originalFilename = file.getOriginalFilename();
//                        if (originalFilename != null && originalFilename.contains(".")) {
//                            return originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
//                        }
//                        return "bin"; // 기본 확장자
//                    }
//                })
//                .orElse("bin");
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
//        log.info("Portfolio file uploaded to Supabase bucket '{}': {}", bucketName, filename);
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