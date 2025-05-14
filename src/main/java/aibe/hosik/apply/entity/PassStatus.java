package aibe.hosik.apply.entity;

import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
public enum PassStatus {
  PASS, FAIL, PENDING
}
