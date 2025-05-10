package aibe.hosik.resume.service;

import aibe.hosik.resume.dto.ResumeDTO;
import aibe.hosik.resume.entity.Resume;
import aibe.hosik.resume.repository.ResumeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {
  private final ResumeRepository resumeRepository;

  public List<ResumeDTO> getResumeByUserId(Long userId){
    List<Resume> resumes = resumeRepository.findByUserId(userId);

    // DTO의 정적 팩토리 메서드 활용하여 변환
    return resumes.stream()
            .map(ResumeDTO::from)
            .collect(Collectors.toList());
  }
}
