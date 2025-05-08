package aibe.hosik.post.dto;

import aibe.hosik.post.entity.Post;
import aibe.hosik.post.entity.PostCategory;
import aibe.hosik.post.entity.PostType;
import aibe.hosik.user.User;

import java.time.LocalDate;
import java.util.List;

public record PostRequestDTO(
        String title,
        String content,
        Integer headCount,
        String image,
        String requirementPersonality,
        LocalDate endedAt,

        PostCategory category,
        PostType type,

        List<String> skills
) {
    public Post toEntity(User user) {
        return Post.builder()
                .title(title())
                .content(content())
                .headCount(headCount())
                .image(image())
                .requirementPersonality(requirementPersonality())
                .endedAt(endedAt())
                .category(category())
                .type(type())
                .user(user)
                .build();
    }
}
