package aibe.hosik.post.dto;

import aibe.hosik.post.entity.Post;

import java.util.List;

public record MatchedUserDTO(
        Long userId,
        String username,
        String nickname,
        String image,
        String introduction
) {

}
