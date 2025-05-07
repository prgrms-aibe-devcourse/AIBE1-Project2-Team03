package aibe.hosik.model.repository;

import aibe.hosik.model.entity.LocalUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 로컬 회원 정보를 DB에서 조회/저장하는 리포지토리 인터페이스
 */
@Repository
public interface LocalUserRepository extends JpaRepository<LocalUser, Long> {

    Optional<LocalUser> findByUsername(String username);

    Optional<Object> findByEmail(@Email(message = "유효한 이메일 형식이어야 합니다.") @NotBlank(message = "이메일은 필수 입력값입니다.") String email);
}
