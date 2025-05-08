package aibe.hosik.post.dto;

import aibe.hosik.post.entity.Post;
import aibe.hosik.post.entity.PostCategory;
import aibe.hosik.post.entity.PostType;

import java.time.LocalDate;
import java.util.List;

public record PostDetailDTO(
        Long id,
        String title,
        String content,
        Integer headCount,
        String image,
        String requirementPersonality,
        LocalDate endedAt,

        String category,
        String type,

        List<String> skills,

        // 현재 선택된 목록 보여주기
        List<MatchedUserDTO> matchedUsers,
        int currentCount
) {
    public static PostDetailDTO from(Post post, List<String> skills, List<MatchedUserDTO> matchedUsers, int currentCount) {
    return new PostDetailDTO(
            post.getId(),
            post.getTitle(),
            post.getContent(),
            post.getHeadCount(),
            post.getImage(),
            post.getRequirementPersonality(),
            post.getEndedAt(),
            post.getCategory().toString(),
            post.getType().toString(),
            skills,
            matchedUsers,
            currentCount
    );
}}
