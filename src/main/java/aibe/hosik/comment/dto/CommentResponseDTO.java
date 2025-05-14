package aibe.hosik.comment.dto;

import aibe.hosik.comment.entity.Comment;
import aibe.hosik.post.dto.PostResponseDTO;
import aibe.hosik.post.entity.Post;
import aibe.hosik.profile.Profile;
import aibe.hosik.profile.dto.ProfileResponse;
import aibe.hosik.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record CommentResponseDTO(
        Long id,
        String content,
        LocalDateTime createdAt,
        ProfileResponse profile,
        // 부모 댓글 > 자식 댓글
        List<CommentResponseDTO> replies
) {
    public static CommentResponseDTO from(Comment comment) {
        User user = comment.getUser();
        ProfileResponse profile = ProfileResponse.from(comment.getUser().getProfile());
        return new CommentResponseDTO(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                profile,
                new ArrayList<>()
        );
    }
}


