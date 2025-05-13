package aibe.hosik.handler.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  NOT_FOUND_POST(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
  NOT_FOUND_USER(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
  NOT_FOUND_RESUME(HttpStatus.NOT_FOUND, "자기소개서를 찾을 수 없습니다."),
  NOT_FOUND_PROFILE(HttpStatus.NOT_FOUND, "프로필을 찾을 수 없습니다."),
  NOT_FOUND_REVIEW(HttpStatus.NOT_FOUND, "후기를 찾을 수 없습니다."),

  LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),

  S3_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3 업로드에 실패했습니다.");

  private final HttpStatus statusCode;
  private final String message;
}
