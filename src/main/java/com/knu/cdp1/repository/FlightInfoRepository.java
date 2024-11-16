package com.knu.cdp1.repository;

import com.knu.cdp1.model.FlightInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FlightInfoRepository extends JpaRepository<FlightInfo, Long> {

    //RAND()로 생성된 값을 11로 곱하여 0 이상 11 미만의 소수를 생성
    @Modifying
    @Query(value = "UPDATE flight_info SET is_delayed = FLOOR(RAND() * 11) WHERE is_delayed = 0 AND (rainfall != 0 OR wind_speed > 20)", nativeQuery = true)
    void updateIsDelayedRandomly();



}
