package aibe.hosik.auth.service;

import aibe.hosik.auth.model.entity.LocalUser;
import aibe.hosik.auth.model.entity.SocialUser;
import aibe.hosik.auth.model.entity.Role;
import aibe.hosik.auth.model.repository.LocalUserRepository;
import aibe.hosik.auth.model.repository.SocialUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Log
public class CustomUserDetailsService implements UserDetailsService {

    private final SocialUserRepository socialUserRepository;
    private final LocalUserRepository localUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isEmpty()) {
            throw new UsernameNotFoundException("유저명이 제공되지 않았습니다.");
        }

        // 소셜 로그인 유저 처리
        if (username.startsWith("github_") || username.startsWith("kakao_")) {
            SocialUser socialUser = socialUserRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("소셜 유저를 찾을 수 없습니다: " + username));

            return org.springframework.security.core.userdetails.User.builder()
                    .username(socialUser.getUsername())
                    .password("")  // 소셜 로그인 유저는 비밀번호가 없음
                    .authorities(
                            Collections.singleton(
                                    new SimpleGrantedAuthority(socialUser.getRole().name())
                            )
                    )
                    .build();
        }

        // 로컬 로그인 유저 처리
        LocalUser localUser = localUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("로컬 유저를 찾을 수 없습니다: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(localUser.getUsername())
                .password(localUser.getPassword())
                .authorities(
                        Collections.singleton(
                                new SimpleGrantedAuthority(localUser.getRoles().name())
                        )
                )
                .build();
    }
}