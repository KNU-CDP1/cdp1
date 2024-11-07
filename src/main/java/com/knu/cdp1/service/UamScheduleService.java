package com.knu.cdp1.service;

import com.knu.cdp1.model.FlightInfo;
import com.knu.cdp1.model.Settings;
import com.knu.cdp1.repository.FlightInfoRepository;
import com.knu.cdp1.repository.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class UamScheduleService {

    @Autowired
    private FlightInfoRepository flightInfoRepository;

    @Autowired
    private SettingsRepository settingsRepository;

    public List<FlightInfo> saveFlightsFromCsv(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            reader.readLine(); // 첫 줄 (헤더) 건너뛰기
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                FlightInfo flight = new FlightInfo();
                flight.setFlightNumber(data[0]);
                flight.setPassengers(Integer.parseInt(data[1]));
                flight.setSeats(Integer.parseInt(data[2]));
                flight.setCost(Double.parseDouble(data[3]));

                // 추가된 필드에 맞춰 데이터 초기화
                flight.setPlannedStart(Integer.parseInt(data[4]));
                flight.setPlannedEnd(Integer.parseInt(data[5]));
                flight.setWeather(data[6]);
                flight.setInFlight(data[8].equalsIgnoreCase("In flight"));

                // seatCost는 각 비행 편마다 다를 수 있으며 필요에 따라 추가적인 로직으로 설정
                flight.setSeatCost(Double.parseDouble(data[9]));

                // 초기 상태값 (계산 후 업데이트됨)
                flight.setDelayTime(0);
                flight.setCancelled(false);
                flight.setAdjustedStart(flight.getPlannedStart());
                flight.setAdjustedEnd(flight.getPlannedEnd());
                flight.setRisk(calculateWeatherRisk(flight));
                flightInfoRepository.save(flight);
            }

            this.calculateSchedule();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
        }
        return flightInfoRepository.findAll(); // 모든 비행 정보 반환
    }



    public List<FlightInfo> calculateSchedule() {
        Settings settings = settingsRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Settings not found"));
        List<FlightInfo> flights = flightInfoRepository.findAll();

        double M = 1_000_000; // 큰 값 (제약 조건을 처리하는 데 사용)
        double lambdaRisk = settings.getWeatherRiskWeight();

        for (int i = 0; i < flights.size(); i++) {
            FlightInfo flight = flights.get(i);
            double delayPenalty = flight.getPassengers() * flight.getSeatCost() * 50;
            double cancelPenalty = flight.getPassengers() * flight.getSeatCost() * 500;

            // 날씨 위험도 계산
            double baseWeatherRisk = calculateWeatherRisk(flight);
            boolean isCancelled = false;
            int delayTime = calculateInitialDelay(flight);

            // 지연 및 취소 비용 비교
            double delayCost = delayPenalty * delayTime + lambdaRisk * baseWeatherRisk * delayTime;
            double cancelCost = cancelPenalty;

            // 최적화 결정
            if (cancelCost < delayCost) {
                isCancelled = true;
                delayTime = 0;
            } else {
                // 제약 조건 1: 지연 시 비행 간 시간 차이 유지
                if (i > 0) {
                    FlightInfo previousFlight = flights.get(i - 1);
                    if ((previousFlight.getPlannedEnd() + delayTime + 3 > flight.getPlannedStart() + delayTime) && !isCancelled) {
                        delayTime += 3;
                    }
                }
                // 제약 조건 2: 취소 여부와 지연은 상호 배타적
                if (isCancelled) {
                    delayTime = 0;
                }

                // 제약 조건 3: 특정 비행은 취소되지 않도록 설정
                if (flight.isInFlight()) {
                    isCancelled = false;
                }

                // 제약 조건 4: 날씨 위험도가 0.5 이하이어야 함
                double weatherChange = calculateWeatherRiskChange(flight);
                double adjustedRisk = baseWeatherRisk + weatherChange * delayTime;
                if (adjustedRisk > 0.5) {
                    isCancelled = true;
                    delayTime = 0;
                }
            }

            // 최적화된 출발 및 도착 시간 계산
            int adjustedStart = isCancelled ? -1 : flight.getPlannedStart() + delayTime;
            int adjustedEnd = isCancelled ? -1 : flight.getPlannedEnd() + delayTime;

            // FlightInfo 업데이트
            flight.setCost(isCancelled ? cancelPenalty : delayCost);
            flight.setCancelled(isCancelled);
            flight.setDelayTime(delayTime);
            flight.setAdjustedStart(adjustedStart);
            flight.setAdjustedEnd(adjustedEnd);
            flight.setRisk(baseWeatherRisk);

            flightInfoRepository.save(flight);
        }
        return flights;
    }

    private double calculateWeatherRisk(FlightInfo flight) {
        double windSpeed = flight.getWindSpeed();
        double rainfall = flight.getRainfall();
        double visibility = flight.getVisibility();

        // visibility가 0일 경우 기본값 설정
        if (visibility == 0) {
            visibility = 1; // 0을 피하기 위해 기본값 설정 (예: 1)
        }

        return (0.4 * windSpeed / 30) + (0.4 * rainfall / 30) + (0.2 * 500 / visibility);
    }


    private double calculateWeatherRiskChange(FlightInfo flight) {
        // 날씨 변화에 따른 위험도 변화를 추정하여 반영
        return 0.1; // 예시 값
    }

    private int calculateInitialDelay(FlightInfo flight) {
        return flight.isDelayed() ? 6 : 0;  // 지연이 발생한 경우 6분 추가 지연
    }
}
