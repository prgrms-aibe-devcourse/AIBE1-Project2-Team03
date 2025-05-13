package aibe.hosik.review;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
  List<Review> findAllByRevieweeId(Long userId);

  void deleteByIdAndReviewerId(Long id, Long reviewerId);
}
