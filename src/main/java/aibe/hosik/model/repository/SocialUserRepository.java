package aibe.hosik.model.repository;

import aibe.hosik.model.entity.SocialUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialUserRepository extends JpaRepository<SocialUser, Long> {
    Optional<SocialUser> findByUsername(String username);
}