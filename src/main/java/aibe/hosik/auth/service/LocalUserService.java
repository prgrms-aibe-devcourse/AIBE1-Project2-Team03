package aibe.hosik.service;

import aibe.hosik.dto.SignUpRequest;
import aibe.hosik.model.entity.LocalUser;
import aibe.hosik.model.repository.LocalUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocalUserService {
    private final LocalUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(SignUpRequest req) {
        userRepository.findByUsername(req.getUsername())
                .ifPresent(u -> { throw new IllegalArgumentException("이미 사용 중인 아이디입니다."); });
        userRepository.findByEmail(req.getEmail())
                .ifPresent(u -> { throw new IllegalArgumentException("이미 사용 중인 이메일입니다."); });

        LocalUser user = LocalUser.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .email(req.getEmail())
                .build();

        userRepository.save(user);
    }
}
