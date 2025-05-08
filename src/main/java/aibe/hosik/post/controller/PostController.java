package aibe.hosik.post.controller;

import aibe.hosik.post.dto.*;
import aibe.hosik.post.entity.Post;
import aibe.hosik.post.entity.PostCategory;
import aibe.hosik.post.entity.PostType;
import aibe.hosik.post.facade.PostFacade;
import aibe.hosik.post.service.PostService;
import aibe.hosik.skill.repository.PostSkillRepository;
import aibe.hosik.user.User;
import aibe.hosik.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Post", description = "모집글 API") // Swagger Tag
public class PostController {
  private final PostFacade postFacade;

  @SecurityRequirement(name = "JWT")
  @Operation(summary = "모집글 등록", description = "모집글을 등록합니다.")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> createPost(
          @RequestParam("title") String title,
          @RequestParam("content") String content,
          @RequestParam("headCount") Integer headCount,
          @RequestParam("requirementPersonality") String requirementPersonality,
          @RequestParam("endedAt") String endedAt,
          @RequestParam("category") String category,
          @RequestParam("type") String type,
          @RequestParam("skills") List<String> skills,
          @RequestParam(value = "image", required = false) MultipartFile image,
          @AuthenticationPrincipal User user) {

    if (user == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }

    // String으로 받은 파라미터를 적절한 타입으로 변환
    PostCategory postCategory;
    PostType postType;
    LocalDate endDate;

    try {
      postCategory = PostCategory.valueOf(category);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              "유효하지 않은 카테고리입니다. 사용 가능한 값: " + Arrays.toString(PostCategory.values()));
    }

    try {
      postType = PostType.valueOf(type);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              "유효하지 않은 타입입니다. 사용 가능한 값: " + Arrays.toString(PostType.values()));
    }

    try {
      endDate = LocalDate.parse(endedAt);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              "날짜 형식이 잘못되었습니다. 형식: YYYY-MM-DD (예: 2025-12-31)");
    }

    // RequestParam 값을 DTO로 변환
    PostRequestDTO dto = new PostRequestDTO(
            title,
            content,
            headCount,
            requirementPersonality,
            endDate,
            postCategory,
            postType,
            skills
    );

    PostResponseDTO responseDTO = postFacade.createPost(dto, image, user);
    return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
  }


  @Operation(summary="모집글 조회", description = "모집글 목록을 조회합니다.")
  @GetMapping
  public ResponseEntity<List<PostResponseDTO>> getAllPosts(){
    return ResponseEntity.ok(postFacade.getAllPosts());
  }

  @Operation(summary="모집글 상세 조회", description="모집글 게시글을 상세 조회합니다")
  @GetMapping("/{postId}")
  public ResponseEntity<PostDetailDTO> getPostDetail(@PathVariable Long postId){
    return ResponseEntity.ok(postFacade.getPostDetail(postId));
  }

  @SecurityRequirement(name = "JWT")
  @Operation(summary="모집글 삭제", description ="작성자는 모집글을 삭제합니다")
  @DeleteMapping("/{postId}")
  public ResponseEntity<?> deletePost(@PathVariable Long postId, @AuthenticationPrincipal User user){
    postFacade.deletePost(postId, user);
    return ResponseEntity.noContent().build();
  }

  @SecurityRequirement(name = "JWT")
  @Operation(summary="모집글 수정", description="모집글을 수정합니다.")
  @PatchMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> updatePost(@PathVariable Long postId,
                                      @RequestPart("dto") PostPatchDTO dto,
                                      @RequestPart(value="image") MultipartFile image,
                                      @AuthenticationPrincipal User user) {
    PostResponseDTO responseDTO = postFacade.updatePost(postId, dto, image, user);
    return ResponseEntity.ok(responseDTO);
  }
}
