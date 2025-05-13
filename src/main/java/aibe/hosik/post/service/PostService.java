package aibe.hosik.post.service;

import aibe.hosik.post.dto.*;
import aibe.hosik.user.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {
  List<PostResponseDTO> getAllPosts();
  List<PostResponseDTO> getAllPostsCreatedByAuthor(Long userId);
  List<PostResponseDTO> getAllPostsJoinedByUser(Long userId);
  List<PostTogetherDTO> getAllPostsByTogether(Long revieweeId, User user);
  PostResponseDTO createPost(PostRequestDTO dto, MultipartFile image, User user);
  PostDetailDTO getPostDetail(Long postId);
  void deletePost(Long postId, User user);
  PostResponseDTO updatePost(Long postId, PostPatchDTO dto, MultipartFile image, User user);
}
