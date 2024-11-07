package com.knu.cdp1.repository;

import com.knu.cdp1.model.FlightInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightInfoRepository extends JpaRepository<FlightInfo, Long> {
}
