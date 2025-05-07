package aibe.hosik.apply.controller;

import aibe.hosik.apply.service.ApplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/applies")
@RequiredArgsConstructor
public class ApplyController {
  private final ApplyService applyService;
}
