package aibe.hosik.auth.service;

import aibe.hosik.user.User;
import aibe.hosik.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Log
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isEmpty()) {
            throw new UsernameNotFoundException("유저명이 제공되지 않았습니다.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warning("사용자를 찾을 수 없습니다: " + username);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
                });

        return user;
    }
}
