package aibe.hosik.resume;

import aibe.hosik.common.dto.ApiResponse;
import aibe.hosik.common.exception.ResourceNotFoundException;
import aibe.hosik.resume.dto.ResumeDTO;
import aibe.hosik.resume.dto.ResumePortfolioRequest;
import aibe.hosik.resume.dto.ResumeRequest;
import aibe.hosik.resume.entity.Resume;
import aibe.hosik.user.User;
import aibe.hosik.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@Tag(name = "Resume", description = "자기소개서 관리 API")
@SecurityRequirement(name = "JWT")
public class ResumeController {
  private final ResumeService resumeService;
  private final UserRepository userRepository;

  /**
   * 자기소개서 목록 조회
   */
  @Operation(summary = "자기소개서 목록 조회", description = "페이지네이션을 적용하여 사용자의 자기소개서 목록을 조회합니다.")
  @ApiResponses(value = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
  })
  @GetMapping
  public ResponseEntity<Page<ResumeDTO>> getResumes(
          @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
          @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
          @AuthenticationPrincipal User user) {

    Pageable pageable = PageRequest.of(page, size);
    Page<Resume> resumes = resumeService.getUserResumes(user.getId(), pageable);

    Page<ResumeDTO> resumeDTOs = resumes.map(ResumeDTO::from);
    return ResponseEntity.ok(resumeDTOs);
  }

  /**
   * 자기소개서 상세 조회
   */
  @Operation(summary = "자기소개서 상세 조회", description = "특정 자기소개서의 상세 정보를 조회합니다.")
  @ApiResponses(value = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "자기소개서를 찾을 수 없음")
  })
  @GetMapping("/{resumeId}")
  public ResponseEntity<ResumeDTO> getResume(@PathVariable Long resumeId) {
    Resume resume = resumeService.getResume(resumeId);
    return ResponseEntity.ok(ResumeDTO.from(resume));
  }

  /**
   * 자기소개서 등록
   */
  @Operation(summary = "자기소개서 등록", description = "새로운 자기소개서를 등록합니다.")
  @ApiResponses(value = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
  })
  @PostMapping
  public ResponseEntity<ResumeDTO> createResume(@RequestBody ResumeRequest request, @AuthenticationPrincipal User user) {
    Resume resume = resumeService.createResume(
            user.getId(),
            request.title(),
            request.content(),
            request.personality(),
            request.portfolio(),
            request.isMain()
    );

    log.info("Created resume ID: {} for user: {}", resume.getId(), user.getEmail());
    return ResponseEntity.status(HttpStatus.CREATED).body(ResumeDTO.from(resume));
  }

  /**
   * 자기소개서 수정
   */
  @Operation(summary = "자기소개서 수정", description = "기존 자기소개서를 수정합니다.")
  @ApiResponses(value = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "자기소개서를 찾을 수 없음")
  })
  @PutMapping("/{resumeId}")
  public ResponseEntity<ResumeDTO> updateResume(
          @PathVariable Long resumeId,
          @RequestBody ResumeRequest request,
          @AuthenticationPrincipal User user) {

    Resume updatedResume = resumeService.updateResume(
            resumeId,
            user.getId(),
            request.title(),
            request.content(),
            request.personality(),
            request.portfolio(),
            request.isMain()
    );

    log.info("Updated resume ID: {} for user: {}", resumeId, user.getEmail());
    return ResponseEntity.ok(ResumeDTO.from(updatedResume));
  }

  /**
   * 포트폴리오 파일 업로드
   */
  @Operation(summary = "포트폴리오 파일 업로드", description = "자기소개서에 포트폴리오 파일을 업로드합니다.")
  @ApiResponses(value = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "업로드 성공"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
  })
  @PostMapping(value = "/{resumeId}/portfolio-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> uploadPortfolioFile(
          @PathVariable Long resumeId,
          @RequestParam("file") MultipartFile file,
          @AuthenticationPrincipal User user) {

    try {
      Resume resume = resumeService.updatePortfolioFile(resumeId, user.getId(), file);
      log.info("Uploaded portfolio file for resume ID: {} by user: {}", resumeId, user.getEmail());
      return ResponseEntity.ok(ResumeDTO.from(resume));
    } catch (Exception e) {
      log.error("Error uploading portfolio file for resume ID: {} by user {}: {}",
              resumeId, user.getEmail(), e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  /**
   * 포트폴리오 URL 업데이트
   */
  @Operation(summary = "포트폴리오 URL 업데이트", description = "자기소개서에 포트폴리오 URL을 업데이트합니다.")
  @ApiResponses(value = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "업데이트 성공"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
  })
  @PutMapping("/{resumeId}/portfolio")
  public ResponseEntity<?> updatePortfolio(
          @PathVariable Long resumeId,
          @RequestBody ResumePortfolioRequest request,
          @AuthenticationPrincipal User user) {

    try {
      Resume resume = resumeService.updatePortfolio(resumeId, user.getId(), request.portfolio());
      log.info("Updated portfolio for resume ID: {} by user: {}", resumeId, user.getEmail());
      return ResponseEntity.ok(ResumeDTO.from(resume));
    } catch (Exception e) {
      log.error("Error updating portfolio for resume ID: {} by user {}: {}",
              resumeId, user.getEmail(), e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  /**
   * 자기소개서 삭제
   */
  @Operation(summary = "자기소개서 삭제", description = "특정 자기소개서를 삭제합니다.")
  @ApiResponses(value = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "자기소개서를 찾을 수 없음")
  })
  @DeleteMapping("/{resumeId}")
  public ResponseEntity<?> deleteResume(
          @PathVariable Long resumeId,
          @AuthenticationPrincipal User user) {

    resumeService.deleteResume(resumeId, user.getId());
    log.info("Deleted resume ID: {} by user: {}", resumeId, user.getEmail());

    return ResponseEntity.ok(new ApiResponse(true, "자기소개서가 성공적으로 삭제되었습니다."));
  }

  /**
   * 최근 자기소개서 목록 조회
   */
  @Operation(summary = "최근 자기소개서 목록 조회", description = "사용자의 최근 자기소개서 목록을 조회합니다.")
  @ApiResponses(value = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
  })
  @GetMapping("/recent")
  public ResponseEntity<List<ResumeDTO>> getRecentResumes(
          @Parameter(description = "조회할 자기소개서 개수") @RequestParam(defaultValue = "5") int limit,
          @AuthenticationPrincipal User user) {

    List<Resume> recentResumes = resumeService.getRecentResumes(user.getId(), limit);
    List<ResumeDTO> resumeDTOs = recentResumes.stream()
            .map(ResumeDTO::from)
            .collect(Collectors.toList());

    return ResponseEntity.ok(resumeDTOs);
  }

  /**
   * 대표 자기소개서 조회
   */
  @Operation(summary = "대표 자기소개서 조회", description = "사용자의 대표 자기소개서를 조회합니다.")
  @ApiResponses(value = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "대표 자기소개서를 찾을 수 없음")
  })
  @GetMapping("/main")
  public ResponseEntity<ResumeDTO> getMainResume(@AuthenticationPrincipal User user) {
    try {
      Resume mainResume = resumeService.getMainResume(user.getId());
      return ResponseEntity.ok(ResumeDTO.from(mainResume));
    } catch (ResourceNotFoundException e) {
      log.error("Main resume not found for user: {}", user.getEmail());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(null);
    }
  }

  /**
   * 대표 자기소개서로 설정
   */
  @Operation(summary = "대표 자기소개서로 설정", description = "특정 자기소개서를 대표 자기소개서로 설정합니다.")
  @ApiResponses(value = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "설정 성공"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "자기소개서를 찾을 수 없음")
  })
  @PutMapping("/{resumeId}/main")
  public ResponseEntity<ResumeDTO> setAsMainResume(
          @PathVariable Long resumeId,
          @AuthenticationPrincipal User user) {

    Resume mainResume = resumeService.setAsMainResume(resumeId, user.getId());
    log.info("Set resume ID: {} as main for user: {}", resumeId, user.getEmail());

    return ResponseEntity.ok(ResumeDTO.from(mainResume));
  }

  /**
   * 포트폴리오 자기소개서 목록 조회
   */
  @Operation(summary = "포트폴리오 자기소개서 목록 조회", description = "사용자의 포트폴리오가 있는 자기소개서 목록을 조회합니다.")
  @ApiResponses(value = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
  })
  @GetMapping("/portfolios")
  public ResponseEntity<List<ResumeDTO>> getUserPortfolios(@AuthenticationPrincipal User user) {
    List<Resume> portfolios = resumeService.getUserPortfolios(user.getId());
    List<ResumeDTO> portfolioDTOs = portfolios.stream()
            .map(ResumeDTO::from)
            .collect(Collectors.toList());

    return ResponseEntity.ok(portfolioDTOs);
  }

  /**
   * 자기소개서 검색
   */
  @Operation(summary = "자기소개서 검색", description = "제목으로 자기소개서를 검색합니다.")
  @ApiResponses(value = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "검색 성공"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
  })
  @GetMapping("/search")
  public ResponseEntity<List<ResumeDTO>> searchResumesByTitle(
          @Parameter(description = "검색 키워드") @RequestParam String keyword,
          @AuthenticationPrincipal User user) {

    List<Resume> foundResumes = resumeService.searchResumesByTitle(user.getId(), keyword);
    List<ResumeDTO> resumeDTOs = foundResumes.stream()
            .map(ResumeDTO::from)
            .collect(Collectors.toList());

    log.info("Searched resumes with keyword '{}' for user: {}, found: {}",
            keyword, user.getEmail(), foundResumes.size());

    return ResponseEntity.ok(resumeDTOs);
  }
}