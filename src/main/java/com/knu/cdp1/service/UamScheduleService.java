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
                flight.setDeparture(data[4]);
                flight.setArrival(data[5]);
                flight.setWeather(data[6]);
                flight.setRisk(Integer.parseInt(data[7]));
                flight.setStatus(data[8]);
                flightInfoRepository.save(flight);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
        }
        return flightInfoRepository.findAll(); // 모든 비행 정보 반환
    }

    public List<FlightInfo> calculateSchedule() {
        Settings settings = settingsRepository.findById(1L).orElseThrow(() -> new RuntimeException("Settings not found"));
        List<FlightInfo> flights = flightInfoRepository.findAll();

        for (FlightInfo flight : flights) {
            double baseCost = flight.getCost();
            double risk = 0.0;

            if (flight.getWeather().contains("Rain")) {
                risk += 30;
            } else if (flight.getWeather().contains("Windy")) {
                risk += 20;
            } else if (flight.getWeather().contains("Cloudy")) {
                risk += 10;
            }

            int delayTime = calculateDelayTime(flight);
            if (delayTime > 0) {
                baseCost += delayTime * settings.getDelayPenalty();
                risk += delayTime * 0.1;
            }

            flight.setCost(baseCost);
            flight.setRisk((int) (risk * settings.getWeatherRiskWeight()));
            flightInfoRepository.save(flight); // 업데이트된 정보 저장
        }
        return flights;
    }

    private int calculateDelayTime(FlightInfo flight) {
        if (flight.getDeparture().equals("3:02 PM")) {
            return 20;
        }
        return 0;
    }
}
