package com.knu.cdp1.service;

import com.knu.cdp1.model.FlightInfo;
import com.knu.cdp1.model.Settings;
import com.knu.cdp1.repository.FlightInfoRepository;
import com.knu.cdp1.repository.SettingsRepository;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.PointValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
                flight.setSeatCost(Double.parseDouble(data[9]));

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
        return flightInfoRepository.findAll();
    }

    public List<FlightInfo> calculateSchedule() {
        Settings settings = settingsRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Settings not found"));
        List<FlightInfo> flights = flightInfoRepository.findAll();

        List<LinearConstraint> constraints = new ArrayList<>();
        double lambdaRisk = settings.getWeatherRiskWeight();
        int N = flights.size();
        double M = 1_000_000; // 큰 값으로 상호 배타성 제약 조건을 처리

        double[] objectiveCoefficients = new double[N * 2]; // 지연(d)와 취소(z) 변수 각각 N개씩

        // 목적 함수 계수 설정 및 페널티 계산
        for (int i = 0; i < N; i++) {
            FlightInfo flight = flights.get(i);
            double delayPenalty = flight.getPassengers() * flight.getSeatCost() * 50;
            double cancelPenalty = flight.getPassengers() * flight.getSeatCost() * 500;
            double baseWeatherRisk = calculateWeatherRisk(flight);

            objectiveCoefficients[i] = delayPenalty + lambdaRisk * baseWeatherRisk;  // 지연 페널티
            objectiveCoefficients[i + N] = cancelPenalty;  // 취소 페널티

            // 지연-취소 상호 배타성 제약 조건 (취소 시 지연 시간이 0이 되도록)
            double[] delayCancelConstraint = new double[N * 2];
            delayCancelConstraint[i] = 1;
            delayCancelConstraint[i + N] = M;
            constraints.add(new LinearConstraint(delayCancelConstraint, Relationship.LEQ, M));
        }

        // 제약 조건 1: 각 비행편 간 출발 시간 최소 3분 간격
        for (int i = 0; i < N - 1; i++) {
            double[] startTimeGapConstraint = new double[N * 2];
            startTimeGapConstraint[i] = 1;
            startTimeGapConstraint[i + 1] = -1;
            constraints.add(new LinearConstraint(startTimeGapConstraint, Relationship.LEQ, -3));
        }

        // 제약 조건 3: 특정 비행은 취소되지 않도록 설정
        for (int i = 0; i < N; i++) {
            if (flights.get(i).isInFlight()) {
                double[] inFlightConstraint = new double[N * 2];
                inFlightConstraint[i + N] = 1;
                constraints.add(new LinearConstraint(inFlightConstraint, Relationship.EQ, 0));
            }
        }

        // 제약 조건 4: 날씨 위험도는 0.5 이하로 제한
        for (int i = 0; i < N; i++) {
            FlightInfo flight = flights.get(i);
            double weatherChange = calculateWeatherRiskChange(flight);
            double[] weatherRiskConstraint = new double[N * 2];
            weatherRiskConstraint[i] = weatherChange;
            constraints.add(new LinearConstraint(weatherRiskConstraint, Relationship.LEQ, 0.5 - calculateWeatherRisk(flight)));
        }

        // SimplexSolver를 사용하여 선형 계획법 문제 해결
        LinearObjectiveFunction objectiveFunction = new LinearObjectiveFunction(objectiveCoefficients, 0);
        SimplexSolver solver = new SimplexSolver();

        try {
            PointValuePair solution = solver.optimize(new MaxIter(100),
                    new LinearConstraintSet(constraints),
                    GoalType.MINIMIZE,
                    new NonNegativeConstraint(true));
            double[] results = solution.getPoint();

            // 최적화 결과를 각 FlightInfo에 반영
            for (int i = 0; i < N; i++) {
                FlightInfo flight = flights.get(i);
                int delayTime = (int) results[i];
                boolean isCancelled = results[i + N] > 0.5;

                flight.setDelayTime(delayTime);
                flight.setCancelled(isCancelled);

                // 출발 및 도착 시간 설정
                flight.setAdjustedStart(isCancelled ? -1 : flight.getPlannedStart() + delayTime);
                flight.setAdjustedEnd(isCancelled ? -1 : flight.getPlannedEnd() + delayTime);

                // 비용 설정
                flight.setCost(isCancelled ? objectiveCoefficients[i + N] : objectiveCoefficients[i] * delayTime);

                flightInfoRepository.save(flight);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return flights;
    }

    private double calculateWeatherRisk(FlightInfo flight) {
        double windSpeed = flight.getWindSpeed();
        double rainfall = flight.getRainfall();
        double visibility = flight.getVisibility();

        if (visibility == 0) {
            visibility = 1;
        }

        return (0.4 * windSpeed / 30) + (0.4 * rainfall / 30) + (0.2 * 500 / visibility);
    }

    private double calculateWeatherRiskChange(FlightInfo flight) {
        return 0.1; // 예시 값으로, 필요에 따라 조정 가능
    }
}
