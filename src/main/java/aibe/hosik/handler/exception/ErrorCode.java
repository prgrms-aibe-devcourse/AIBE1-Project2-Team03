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
  NOT_FOUND_APPLY(HttpStatus.NOT_FOUND, "지원서를 찾을 수 없습니다."),

  LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),

  POST_AUTHOR_FORBIDDEN(HttpStatus.FORBIDDEN, "모집글 작성자만 접근할 수 있습니다."),
  POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "모집글 작성자만 수정, 삭제할 수 있습니다."),

  RESUME_FORBIDDEN(HttpStatus.FORBIDDEN, "본인의 이력서만 사용할 수 있습니다."),
  APPLY_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "본인의 지원서만 삭제할 수 있습니다."),

  INVALID_DATA_FORMAT(HttpStatus.BAD_REQUEST, "날짜 형식이 잘못되었습니다. 형식: YYYY-MM-DD (예: 2025-12-31)"),
  S3_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3 업로드에 실패했습니다.");

  private final HttpStatus statusCode;
  private final String message;
}
