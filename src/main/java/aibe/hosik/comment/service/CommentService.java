package aibe.hosik.comment.service;

import aibe.hosik.comment.dto.CommentRequest;
import aibe.hosik.comment.dto.CommentResponse;
import aibe.hosik.user.User;

import java.util.List;


public interface CommentService {
  void createComment(CommentRequest dto, User user);
  List<CommentResponse> getCommentsByPostId(Long postId);
  void deleteComment(Long commentId, User user);
  void updateComment(Long commentId, CommentRequest dto, User user);
}
