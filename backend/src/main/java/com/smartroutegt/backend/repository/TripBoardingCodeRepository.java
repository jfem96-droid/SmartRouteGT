package com.smartroutegt.backend.repository;
import com.smartroutegt.backend.entity.TripBoardingCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface TripBoardingCodeRepository extends JpaRepository<TripBoardingCode,Long> {
 Optional<TripBoardingCode> findByCode(String code);
}
