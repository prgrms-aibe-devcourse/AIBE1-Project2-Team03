package aibe.hosik.handler.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  NOT_FOUND_POST(HttpStatus.NOT_FOUND, "post not found"),
  NOT_FOUND_USER(HttpStatus.NOT_FOUND, "post not user"),
  NOT_FOUND_RESUME(HttpStatus.NOT_FOUND, "post not resume")
  ;

  private final HttpStatus statusCode;
  private final String message;
}
