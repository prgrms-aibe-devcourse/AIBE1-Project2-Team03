package aibe.hosik.auth.service;

import aibe.hosik.auth.model.entity.LocalUser;
import aibe.hosik.auth.model.entity.SocialUser;
import aibe.hosik.auth.model.repository.LocalUserRepository;
import aibe.hosik.auth.model.repository.SocialUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log
public class CustomUserDetailsService implements UserDetailsService {
    private final SocialUserRepository socialUserRepository;
    private final LocalUserRepository localUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isEmpty()) {
                    throw new UsernameNotFoundException("유저가 없습니다.");
                }
        if (username.startsWith("github_") || username.startsWith("kakao_")) {
            SocialUser user = socialUserRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("유저가 없습니다: " + username));
            return User.builder()
                    .username(user.getUsername())
                    .password("")
                    .build();
        } else {
            LocalUser account = localUserRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("유저가 없습니다: " + username));
            return User.builder()
                    .username(account.getUsername())
                    .password(account.getPassword())
                    .build();
        }
    }
}