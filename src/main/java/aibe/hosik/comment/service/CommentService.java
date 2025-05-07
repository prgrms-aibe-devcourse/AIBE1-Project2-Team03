package aibe.hosik.comment.service;

import aibe.hosik.comment.dto.CommentRequestDTO;
import aibe.hosik.comment.dto.CommentResponseDTO;
import aibe.hosik.user.User;

import java.util.List;


public interface CommentService {
  void createComment(CommentRequestDTO dto, User user);
  List<CommentResponseDTO> getCommentsByPostId(Long postId);
  void deleteComment(Long commentId, User user);
}
