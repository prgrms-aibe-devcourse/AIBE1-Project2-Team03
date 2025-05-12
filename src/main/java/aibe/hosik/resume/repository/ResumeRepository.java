package aibe.hosik.resume.repository;

import aibe.hosik.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
  List<Resume> findByUserId(Long userId);

  @Query("""
      UPDATE Resume r
      SET isMain = false
      WHERE isMain = true
      """)
  void resetMainResumeFlag();

  Optional<Resume> findByIdAndUserId(Long id, Long userId);

  List<Resume> findAllByUserId(Long userId);

  void deleteByIdAndUserId(Long id, Long userId);
}