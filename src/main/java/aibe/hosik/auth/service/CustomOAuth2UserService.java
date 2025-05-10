package aibe.hosik.auth.service;

import aibe.hosik.user.User;
import aibe.hosik.user.UserRepository;
import aibe.hosik.user.SocialType;
import aibe.hosik.user.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Log
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final UserRepository userRepository;

    @SuppressWarnings("unchecked")
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        Map<String, Object> attributes = oAuth2User.getAttributes();

        String username;
        String name;
        String socialId;
        SocialType type;

        if ("github".equals(registrationId)) {
            if (!attributes.containsKey("login") || !attributes.containsKey(userNameAttributeName)) {
                throw new OAuth2AuthenticationException("GitHub 사용자 정보가 유효하지 않습니다.");
            }
            socialId = attributes.get(userNameAttributeName).toString();
            username = "github_" + attributes.get("login");
            name = (String) attributes.get("name");
            type = SocialType.GITHUB;

        } else if ("kakao".equals(registrationId)) {
            if (!attributes.containsKey(userNameAttributeName)) {
                throw new OAuth2AuthenticationException("Kakao 사용자 정보가 유효하지 않습니다.");
            }
            socialId = attributes.get(userNameAttributeName).toString();
            username = "kakao_" + socialId;
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");
            if (!kakaoProfile.containsKey("nickname")) {
                throw new OAuth2AuthenticationException("Kakao 프로필 정보가 유효하지 않습니다.");
            }
            name = (String) kakaoProfile.get("nickname");
            type = SocialType.KAKAO;

        } else if ("google".equals(registrationId)) {
            if (!attributes.containsKey(userNameAttributeName)) {
                throw new OAuth2AuthenticationException("Google 사용자 정보가 유효하지 않습니다.");
            }
            socialId = attributes.get(userNameAttributeName).toString();
            username = "google_" + socialId;
            name = (String) attributes.get("name");
            type = SocialType.GOOGLE;

        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + registrationId);
        }

        Optional<User> existingUser = userRepository.findBySocialTypeAndSocialId(type, socialId);
        User user = existingUser.orElseGet(() -> {
            log.info("OAuth2 신규 회원가입: " + username);
            User u = User.builder()
                    .username(username)
                    .password(null)
                    .email(null)
                    .name(name)
                    .roles(Role.USER)
                    .socialType(type)
                    .socialId(socialId)
                    .build();
            return userRepository.save(u);
        });

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRoles().name())),
                attributes,
                userNameAttributeName
        );
    }
}
