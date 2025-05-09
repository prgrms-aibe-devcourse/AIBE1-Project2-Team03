package aibe.hosik.handler;

import aibe.hosik.handler.exception.CustomException;
import aibe.hosik.handler.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;

/**
 * 애플리케이션 전역에서 발생하는 예외를 처리하는 핸들러입니다.
 * 다양한 유형의 예외에 대해 일관된 응답 형식을 제공합니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * 처리되지 않은 모든 예외를 처리하는 메서드입니다.
   *
   * @param exception 발생한 예외
   * @param request 클라이언트 요청 정보
   * @return 내부 서버 오류(500) 상태 코드와 에러 응답
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleException(final Exception exception, final HttpServletRequest request) {
    ErrorResponse response = ErrorResponse.of(exception.getMessage());

    log.error(
        "Error ID: {}, Request URL: {}, Message: {}",
        response.errorId(),
        request.getRequestURI(),
        exception.getMessage()
    );

    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(response);
  }

  /**
   * 애플리케이션에서 정의한 사용자 정의 예외를 처리하는 메서드입니다.
   *
   * @param exception 사용자 정의 예외
   * @param request 클라이언트 요청 정보
   * @return 예외에 정의된 상태 코드와 에러 응답
   */
  @ExceptionHandler(CustomException.class)
  public ResponseEntity<?> handleCustomException(final CustomException exception, final HttpServletRequest request) {
    ErrorResponse response = ErrorResponse.of(exception.getMessage());

    log.error(
        "Error ID: {}, Request URL: {}, Message: {}",
        response.errorId(),
        request.getRequestURI(),
        exception.getMessage()
    );

    return ResponseEntity
        .status(exception.getStatusCode())
        .body(response);
  }

  /**
   * Spring의 @Valid 검증 실패 시 발생하는 예외를 처리하는 메서드입니다.
   * 주로 @RequestBody, @ModelAttribute 등에 적용된 검증 실패 시 발생합니다.
   *
   * @param exception @Valid 검증 실패 예외
   * @param request 클라이언트 요청 정보
   * @return 잘못된 요청(400) 상태 코드와 에러 응답
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity<?> handleMethodArgumentNotValidException(
      final MethodArgumentNotValidException exception,
      final HttpServletRequest request
  ) {
    ErrorResponse response = ErrorResponse.of(exception.getMessage());

    log.error(
        "Error ID: {}, Request URL: {}, Message: {}",
        response.errorId(),
        request.getRequestURI(),
        exception.getMessage()
    );

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(response);
  }

  /**
   * 주로 컨트롤러 메서드 파라미터의 유효성 검증 실패 시 발생합니다.
   *
   * @param exception 핸들러 메서드 유효성 검증 예외
   * @param request 클라이언트 요청 정보
   * @return 잘못된 요청(400) 상태 코드와 유효성 검증 오류 정보를 포함한 응답
   */
  @ExceptionHandler(HandlerMethodValidationException.class)
  protected ResponseEntity<?> handleHandlerMethodValidationException(
      final HandlerMethodValidationException exception,
      final HttpServletRequest request
  ) {
    final List<?> validationResults = exception.getAllErrors();

    ErrorResponse response = ErrorResponse.of(exception.getMessage());

    log.error(
        "Error ID: {}, Request URL: {}, Message: {}",
        response.errorId(),
        request.getRequestURI(),
        validationResults
    );

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(response);
  }
}