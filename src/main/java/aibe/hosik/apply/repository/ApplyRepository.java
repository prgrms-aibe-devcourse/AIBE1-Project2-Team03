package aibe.hosik.apply.repository;

import aibe.hosik.apply.entity.Apply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplyRepository extends JpaRepository<Apply, Long> {
    @Query("SELECT a FROM Apply a " +
            "JOIN FETCH a.user u " +
            "JOIN FETCH u.profile p " +
            "WHERE a.post.id = :postId AND a.isSelected = true")
    List<Apply> findWithUserAndProfileByPostId(@Param("postId") Long postId);
    int countByPostIdAndIsSelectedTrue(Long postId);
}
