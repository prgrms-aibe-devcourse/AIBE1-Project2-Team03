package aibe.hosik.service;

import aibe.hosik.model.entity.SocialType;
import aibe.hosik.model.entity.SocialUser;
import aibe.hosik.model.repository.SocialUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Log
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final SocialUserRepository socialUserRepository;

    @SuppressWarnings("unchecked")
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        String username;
        String name;
        SocialType type;

        if ("github".equals(registrationId)) {
            Map<String, Object> attrs = oAuth2User.getAttributes();
            username = "github_" + attrs.get("login");
            name = (String) attrs.get("name");
            type = SocialType.GITHUB;
        } else {
            Map<String, Object> attrs = oAuth2User.getAttributes();
            Map<String, Object> kakaoAccount = (Map<String, Object>) attrs.get("kakao_account");
            Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");
            username = "kakao_" + attrs.get("id").toString();
            name = (String) kakaoProfile.get("nickname");
            type = SocialType.KAKAO;
        }

        SocialUser socialUser = socialUserRepository.findByUsername(username)
                .orElseGet(() -> socialUserRepository.save(
                        SocialUser.builder()
                                .username(username)
                                .name(name)
                                .socialType(type)
                                .build()
                ));

        log.info("OAuth2 login: " + socialUser);
        return oAuth2User;
    }
}