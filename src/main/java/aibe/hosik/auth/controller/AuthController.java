package aibe.hosik.auth.controller;

import aibe.hosik.auth.JwtTokenProvider;
import aibe.hosik.auth.dto.LoginRequest;
import aibe.hosik.auth.dto.PasswordChangeRequest;
import aibe.hosik.auth.dto.SignUpRequest;
import aibe.hosik.auth.dto.SocialLoginRequest;
import aibe.hosik.user.User;
import aibe.hosik.user.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증/인가 API") // Swagger Tag
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwtProvider;

    /**
     * 1) 회원가입
     * POST /auth/signup
     */
    @PostMapping("/signup")
    @Operation(summary = "ID/PW 회원가입")
    public ResponseEntity<?> signup(@Valid @RequestBody SignUpRequest req) {
        userService.register(req);
        return ResponseEntity.ok("회원가입 완료");
    }

    /**
     * 2) 로그인
     * POST /auth/login
     */
    @PostMapping("/login")
    @Operation(summary = "ID/PW 로그인")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
            );
            String token = jwtProvider.generateToken(auth);

            return ResponseEntity.ok(Map.of("token", token));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "잘못된 아이디 또는 비밀번호입니다."));
        } catch (LockedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "계정이 잠겼습니다. 관리자에게 문의하세요."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "로그인 처리 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/login/social")
    @Operation(summary = "소셜 로그인")
    public ResponseEntity<?> socialLogin(@Valid @RequestBody SocialLoginRequest request) {
        User user = userService.socialLogin(request);
        String token = jwtProvider.generateToken(user.getUsername());
        return ResponseEntity.ok(Map.of("token", token));
    }

    /**
     * 3) 비밀번호 변경
     * PATCH /auth/password
     */
    @PatchMapping("/password")
    @Operation(summary = "로컬 회원가입 회원용 비밀번호 변경")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody PasswordChangeRequest req) {
        userService.changePassword(req);
        return ResponseEntity.noContent().build();
    }
}
