package com.smartroutegt.backend.repository;
import com.smartroutegt.backend.entity.PassengerBoarding;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface PassengerBoardingRepository extends JpaRepository<PassengerBoarding,Long> {
 Optional<PassengerBoarding> findFirstByPassengerIdAndClosedFalseOrderByBoardedAtDesc(Long passengerId);
 Optional<PassengerBoarding> findFirstByPassengerIdAndTripIdOrderByBoardedAtDesc(Long passengerId,Long tripId);

 Optional<PassengerBoarding> findFirstByPassengerIdAndSeparatedAtIsNullOrderByBoardedAtDesc(Long passengerId);
}
