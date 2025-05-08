package aibe.hosik.apply.repository;

import aibe.hosik.apply.entity.Apply;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplyRepository extends JpaRepository<Apply, Long> {
    // TODO : post id로 지원서 조회
}
