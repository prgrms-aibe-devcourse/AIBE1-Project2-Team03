package aibe.hosik.apply.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApplyUserResponse {
    private Long userId;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private String introduction;
    private String portfolioUrl;
    private String personality;
}