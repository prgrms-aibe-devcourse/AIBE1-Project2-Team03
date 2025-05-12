package aibe.hosik.resume.service;

import aibe.hosik.handler.exception.CustomException;
import aibe.hosik.handler.exception.ErrorCode;
import aibe.hosik.post.service.StorageService;
import aibe.hosik.resume.dto.ResumeRequest;
import aibe.hosik.resume.dto.ResumeResponse;
import aibe.hosik.resume.entity.Resume;
import aibe.hosik.resume.repository.ResumeRepository;
import aibe.hosik.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ResumeService {
  private final ResumeRepository resumeRepository;
  private final StorageService storageService;

  public void createResume(ResumeRequest request, MultipartFile file, User user) {
    if (request.isMain()) {
      resumeRepository.resetMainResumeFlag();
    }

    String portfolio = storageService.upload(file);

    resumeRepository.save(request.toEntity(portfolio, user));
  }

  public List<ResumeResponse> getAllResumesByUserId(Long userId) {
    return resumeRepository.findAllByUserId(userId)
        .stream()
        .map(ResumeResponse::from)
        .toList();
  }

  public void updateResume(Long resumeId, ResumeRequest request, MultipartFile file, User user) {
    Resume resume = resumeRepository.findByIdAndUserId(resumeId, user.getId())
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RESUME));

    resumeRepository.resetMainResumeFlag();

    Resume mainResume = resume.toBuilder()
        .isMain(true)
        .build();

    resumeRepository.save(mainResume);
  }

  public void deleteResume(Long resumeId, User user) {
    resumeRepository.deleteByIdAndUserId(resumeId, user.getId());
  }
}