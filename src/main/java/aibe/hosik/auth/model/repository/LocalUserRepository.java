package aibe.hosik.auth.model.repository;

import aibe.hosik.auth.model.entity.LocalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 로컬 회원 정보를 DB에서 조회/저장하는 리포지토리 인터페이스
 */
@Repository
public interface LocalUserRepository extends JpaRepository<LocalUser, Long> {

    Optional<LocalUser> findByUsername(String username);

    /** 비밀번호 변경·로그인 시 이메일로 사용자 조회 */
    Optional<LocalUser> findByEmail(String email);
}
