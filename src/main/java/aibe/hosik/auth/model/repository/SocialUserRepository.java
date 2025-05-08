package aibe.hosik.auth.model.repository;

import aibe.hosik.auth.model.entity.SocialUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialUserRepository extends JpaRepository<SocialUser, Long> {
    Optional<SocialUser> findByUsername(String username);
}