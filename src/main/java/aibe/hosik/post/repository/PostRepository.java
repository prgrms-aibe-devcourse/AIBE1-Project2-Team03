package aibe.hosik.post.repository;

import aibe.hosik.post.entity.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    // Post 조회 시 postSkills, skill 엔티티 즉시 로딩 지정
    //@EntityGraph(attributePaths = {"postSkills", "postSkills.skill"})
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.postSkills ps LEFT JOIN FETCH ps.skill")
    List<Post> findAllWithSkills();

    // PostDetail 조회 시 즉시 로딩 지정
    //@EntityGraph(attributePaths = {"postSkills", "postSkills.skill"})
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.postSkills ps LEFT JOIN FETCH ps.skill WHERE p.id = :id")
    Optional<Post> findByIdWithSkills(@Param("id") Long id);
}
