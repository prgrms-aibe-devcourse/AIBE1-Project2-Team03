package aibe.hosik.resume.repository;

import aibe.hosik.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUserId(Long userId);
}