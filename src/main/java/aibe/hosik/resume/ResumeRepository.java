package aibe.hosik.resume;

import org.springframework.data.jpa.repository.JpaRepository;
public interface ResumeRepository extends JpaRepository<Resume, Long> {
}