package com.smartroutegt.backend.repository;
import com.smartroutegt.backend.entity.PassengerFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface PassengerFeedbackRepository extends JpaRepository<PassengerFeedback,Long> {
 Optional<PassengerFeedback> findByBoardingIdAndKind(Long boardingId,String kind);
}
