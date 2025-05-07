package aibe.hosik.comment.dto;

import aibe.hosik.comment.entity.Comment;
import aibe.hosik.post.dto.PostResponseDTO;
import aibe.hosik.post.entity.Post;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record CommentResponseDTO(
        Long id,
        String content,
        String username,
        LocalDateTime createdAt,
        // 부모 댓글 > 자식 댓글
        List<CommentResponseDTO> replies
) {
    public static CommentResponseDTO from(Comment comment) {
        return new CommentResponseDTO(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getName(),
                comment.getCreatedAt(),
                new ArrayList<>()
        );
    }
}


