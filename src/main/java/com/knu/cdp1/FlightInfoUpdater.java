package com.knu.cdp1;

import com.knu.cdp1.controller.UamScheduleController;
import com.knu.cdp1.repository.FlightInfoRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;
@Component
public class FlightInfoUpdater {

    private final UamScheduleController uamScheduleController;

    private final FlightInfoRepository flightInfoRepository;

    public FlightInfoUpdater(FlightInfoRepository flightInfoRepository, UamScheduleController uamScheduleController) {
        this.uamScheduleController = uamScheduleController;
        this.flightInfoRepository = flightInfoRepository;
    }

    @Scheduled(fixedRate = 60000) // 1분마다 실행 (밀리초 단위)
    @Transactional
    public void updateIsDelayed() {

        Random r = new Random();
        if (r.nextInt(3) == 1) {
            //1분마다 1/3 확률로 딜레이 발생
            flightInfoRepository.updateIsDelayedRandomly();
            uamScheduleController.calculateSchedule();
        }
    }
}
