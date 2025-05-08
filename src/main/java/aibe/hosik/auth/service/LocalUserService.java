package aibe.hosik.auth.service;

import aibe.hosik.auth.dto.SignUpRequest;
import aibe.hosik.auth.dto.PasswordChangeRequest;
import aibe.hosik.auth.model.entity.LocalUser;
import aibe.hosik.auth.model.repository.LocalUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocalUserService {
    private final LocalUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** 회원가입 */
    @Transactional
    public void register(SignUpRequest req) {
        userRepository.findByUsername(req.username())
                .ifPresent(u -> { throw new IllegalArgumentException("이미 사용 중인 아이디입니다."); });
        userRepository.findByEmail(req.email())
                .ifPresent(u -> { throw new IllegalArgumentException("이미 사용 중인 이메일입니다."); });

        LocalUser user = LocalUser.builder()
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))
                .email(req.email())
                .build();

        userRepository.save(user);
    }

    /** 비밀번호 변경 */
    @Transactional
    public void changePassword(PasswordChangeRequest req) {
        LocalUser user = userRepository.findByEmail(req.email())
                .orElseThrow(() ->
                        new UsernameNotFoundException("해당 이메일의 사용자를 찾을 수 없습니다: " + req.email())
                );

        // 기존 비밀번호 검증
        if (!passwordEncoder.matches(req.oldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 인코딩 후 저장
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }
}