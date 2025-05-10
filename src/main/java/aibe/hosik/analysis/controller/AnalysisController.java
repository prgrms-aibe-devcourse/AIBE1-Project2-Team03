package aibe.hosik.analysis.controller;

import aibe.hosik.analysis.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/analyses")
@RequiredArgsConstructor
public class AnalysisController {
  private final AnalysisService analysisService;
}
