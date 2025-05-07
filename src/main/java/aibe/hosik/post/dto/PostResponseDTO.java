package aibe.hosik.post.dto;

import aibe.hosik.post.entity.Post;

import java.util.List;

public record PostResponseDTO(
        Long id,
        String image,
        String title,
        String content,
        String category,
        List<String> skills,
        Integer headCount,
        Integer currentCount
) {

    public static PostResponseDTO from(Post post,List<String> skills, Integer currentCount) {
        return new PostResponseDTO(post.getId(),
                post.getImage(),
                post.getTitle(),
                post.getContent(),
                post.getCategory().toString(),
                skills,
                post.getHeadCount(),
                currentCount
        );
    }
}
