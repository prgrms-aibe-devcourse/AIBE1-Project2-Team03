package aibe.hosik.post.facade;

import aibe.hosik.post.dto.PostDetailDTO;
import aibe.hosik.post.dto.PostPatchDTO;
import aibe.hosik.post.dto.PostRequestDTO;
import aibe.hosik.post.dto.PostResponseDTO;
import aibe.hosik.post.service.PostService;
import aibe.hosik.skill.repository.PostSkillRepository;
import aibe.hosik.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PostFacade {
    private final PostService postService;
    private final PostSkillRepository postSkillRepository;

    public PostResponseDTO createPost(PostRequestDTO dto, User user) {
        return postService.createPost(dto, user);
    }

    public List<PostResponseDTO> getAllPosts() {
        return postService.getAllPosts();
    }


    public PostDetailDTO getPostDetail(Long postId) {
        return postService.getPostDetail(postId);
    }
    public void deletePost(Long postId, User user) {
        postService.deletePost(postId, user);
    }

    public PostResponseDTO updatePost(Long postId, PostPatchDTO dto, User user) {
        return postService.updatePost(postId, dto, user);
    }
}
