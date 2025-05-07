package aibe.hosik.post.service;

import aibe.hosik.post.dto.PostDetailDTO;
import aibe.hosik.post.dto.PostPatchDTO;
import aibe.hosik.post.dto.PostRequestDTO;
import aibe.hosik.post.dto.PostResponseDTO;
import aibe.hosik.user.User;

import java.util.List;

public interface PostService {
  List<PostResponseDTO> getAllPosts();
  PostResponseDTO createPost(PostRequestDTO dto, User user);
  PostDetailDTO getPostDetail(Long postId);
  void deletePost(Long postId, User user);
  PostResponseDTO updatePost(Long postId, PostPatchDTO dto, User user);
}
