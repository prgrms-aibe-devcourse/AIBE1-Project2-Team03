package aibe.hosik.comment.dto;

import aibe.hosik.comment.entity.Comment;
import aibe.hosik.post.entity.Post;
import aibe.hosik.user.User;

// 댓글 작성 DTO
public record CommentRequestDTO(
        String content,
        Long postId,
        Long parentCommentId
) {
    public Comment toEntity(Post post, User user, Comment parent) {
        return Comment.builder()
                .content(content)
                .post(post)
                .user(user)
                .parentComment(parent)
                .build();
    }
}
