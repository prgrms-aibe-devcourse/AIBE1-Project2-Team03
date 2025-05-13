package aibe.hosik.profile.dto;

import aibe.hosik.post.dto.PostResponseDTO;
import aibe.hosik.resume.dto.ResumeResponse;
import aibe.hosik.review.dto.ReviewResponse;

import java.util.List;

public record ProfileDetailResponse(
    ProfileResponse profile,
    List<PostResponseDTO> authorPosts,
    List<PostResponseDTO> joinedPosts,
    List<ReviewResponse> reviews,
    List<ResumeResponse> resumes
) {
  public static ProfileDetailResponse from(
      ProfileResponse profile,
      List<PostResponseDTO> authorPosts,
      List<PostResponseDTO> joinedPosts,
      List<ReviewResponse> reviews,
      List<ResumeResponse> resumes
  ) {
    return new ProfileDetailResponse(profile, authorPosts, joinedPosts, reviews, resumes);
  }
}
