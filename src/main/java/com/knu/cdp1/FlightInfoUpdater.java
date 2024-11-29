package com.knu.cdp1;

import com.knu.cdp1.controller.UamScheduleController;
import com.knu.cdp1.repository.FlightInfoRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;
@Component
public class FlightInfoUpdater {

    private int executionCount = 0;

    private final UamScheduleController uamScheduleController;

    private final FlightInfoRepository flightInfoRepository;

    public FlightInfoUpdater(FlightInfoRepository flightInfoRepository, UamScheduleController uamScheduleController) {
        this.uamScheduleController = uamScheduleController;
        this.flightInfoRepository = flightInfoRepository;
    }

    @Scheduled(fixedRate = 10000) // 10초마다 실행 (밀리초 단위)
    @Transactional
    public void updatePosition() {

        if (executionCount%6 == 0){ // 1분마다 실행 (밀리초 단위)
            Random r = new Random();
            if (r.nextInt(3) == 1) {
                //1분마다 1/3 확률로 딜레이 발생
                flightInfoRepository.updateIsDelayedRandomly();
                uamScheduleController.calculateSchedule();
            }
            
        }
        executionCount++;
        uamScheduleController.calculatePosition();
    }
}
