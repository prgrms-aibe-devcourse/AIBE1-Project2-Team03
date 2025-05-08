package aibe.hosik.comment.controller;

import aibe.hosik.comment.dto.CommentRequestDTO;
import aibe.hosik.comment.dto.CommentResponseDTO;
import aibe.hosik.comment.entity.Comment;
import aibe.hosik.comment.service.CommentService;
import aibe.hosik.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "Comment", description = "댓글 API")
public class CommentController {
  private final CommentService commentService;

  @Operation(summary="댓글 등록", description="댓글 및 대댓글을 등록합니다.")
  @PostMapping
  public ResponseEntity<?> createComment(@RequestBody CommentRequestDTO dto, @AuthenticationPrincipal User user) {
    commentService.createComment(dto, user);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @Operation(summary="댓글 조회", description=" 모든 댓글 및 대댓글을 조회합니다.")
  @GetMapping
  public ResponseEntity<List<CommentResponseDTO>> getComments(@RequestParam Long postId){
    List<CommentResponseDTO> comments = commentService.getCommentsByPostId(postId);
    return ResponseEntity.ok(comments);
  }

  @Operation(summary="댓글 삭제", description="댓글을 삭제합니다.")
  @DeleteMapping("/{commentId}")
  public ResponseEntity<?> deleteComment(@PathVariable Long commentId, @AuthenticationPrincipal User user) {
    commentService.deleteComment(commentId, user);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "댓글 수정", description = "댓글을 수정합니다.")
  @PatchMapping("/{commentId}")
  public ResponseEntity<?> updateComment(@PathVariable Long commentId,
                                         @RequestBody CommentRequestDTO dto,
                                         @AuthenticationPrincipal User user) {
    commentService.updateComment(commentId, dto, user);
    return ResponseEntity.ok().build();
  }
}
