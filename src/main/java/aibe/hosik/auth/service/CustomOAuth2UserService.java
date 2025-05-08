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
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

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

                    String username;
                    String name;
                    SocialType type;
                    String socialId;

                    if ("github".equals(registrationId)) {
                        Map<String, Object> attrs = oAuth2User.getAttributes();
                        // 필수 속성(login, name)이 있는지 확인합니다.
                        if (attrs == null || !attrs.containsKey("login") || !attrs.containsKey("name")) {
                            throw new OAuth2AuthenticationException("GitHub 사용자 정보가 유효하지 않습니다.");
                        }
                        socialId = attrs.get("id").toString(); // GitHub ID를 socialId로 사용
                        username = "github_" + attrs.get("login"); // GitHub login을 username으로 사용
                        name = (String) attrs.get("name");
                        type = SocialType.GITHUB;
                    } else if ("kakao".equals(registrationId)) {
                        Map<String, Object> attrs = oAuth2User.getAttributes();
                        // 필수 속성(id)이 있는지 확인합니다.
                        if (attrs == null || !attrs.containsKey("id")) {
                            throw new OAuth2AuthenticationException("Kakao 사용자 정보가 유효하지 않습니다.");
                        }
                        socialId = attrs.get("id").toString(); // Kakao ID를 socialId로 사용
                        username = "kakao_" + socialId; // Kakao ID를 username으로 사용
                        Map<String, Object> kakaoAccount = (Map<String, Object>) attrs.get("kakao_account");
                        // 카카오 계정 및 프로필 정보가 있는지 확인합니다.
                        if (kakaoAccount == null || !kakaoAccount.containsKey("profile")) {
                            throw new OAuth2AuthenticationException("Kakao 계정 정보가 유효하지 않습니다.");
                        }
                        Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");
                        // 카카오 프로필에 닉네임이 있는지 확인합니다.
                        if (kakaoProfile == null || !kakaoProfile.containsKey("nickname")) {
                            throw new OAuth2AuthenticationException("Kakao 프로필 정보가 유효하지 않습니다.");
                        }
                        name = (String) kakaoProfile.get("nickname");
                        type = SocialType.KAKAO;
                    } else {
                        // 지원하지 않는 소셜 로그인 타입인 경우 예외 발생
                        throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + registrationId);
                    }

                    Optional<User> existingUser = userRepository.findBySocialTypeAndSocialId(type, socialId);

                    User user;
                    if (existingUser.isPresent()) {
                        user = existingUser.get();
                        log.info("OAuth2 로그인 성공 (기존 사용자): " + user.getUsername());
                    } else {
                        log.info("OAuth2 회원가입 및 로그인 성공 (신규 사용자): " + username);
                        user = User.builder()
                                .username(username)
                                .password(null)
                                .email(null)
                                .name(name)
                                .roles(Role.USER)
                                .socialType(type)
                                .socialId(socialId)
                                .build();
                        userRepository.save(user);
                    }

                    return oAuth2User;
                }
            }
