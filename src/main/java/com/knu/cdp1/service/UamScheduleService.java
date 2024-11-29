package com.knu.cdp1.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knu.cdp1.model.FlightInfo;
import com.knu.cdp1.model.Settings;
import com.knu.cdp1.model.UploadHistory;
import com.knu.cdp1.repository.FlightInfoRepository;
import com.knu.cdp1.repository.SettingsRepository;
import com.knu.cdp1.repository.UploadHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UamScheduleService {

    @Autowired
    private FlightInfoRepository flightInfoRepository;

    @Autowired
    private SettingsRepository settingsRepository;

    @Autowired
    private UploadHistoryRepository uploadHistoryRepository;


    // 모든 FlightInfo 데이터를 반환하는 메소드
    public List<Map<String, Object>> getAllFlights() {
        List<FlightInfo> flightData = flightInfoRepository.findAll();

        return flightData.stream().map(flight -> {
            Map<String, Object> flightMap = new HashMap<>();
            flightMap.put("id", flight.getId());
            flightMap.put("flightNumber", flight.getFlightNumber());
            flightMap.put("passengers", flight.getPassengers());
            flightMap.put("seats", flight.getSeatCost());
            flightMap.put("date", flight.getDate());
            flightMap.put("weather", flight.getWeather());
            flightMap.put("status", calculateStatus(flight));

            Map <String, String> time = calculateFlightTimes(flight);
            flightMap.put("plannedDeparture", time.get("plannedDeparture"));
            flightMap.put("plannedArrival", time.get("plannedArrival"));
            flightMap.put("adjustedDeparture", time.get("adjustedDeparture"));
            flightMap.put("adjustedArrival", time.get("adjustedArrival"));

            return flightMap;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getScheduleHistory() {
        // UploadHistory 엔티티에서 모든 레코드를 가져와서, 각 레코드를 Map으로 변환하여 리스트로 반환
        return uploadHistoryRepository.findAll().stream().map(uploadHistory -> {
            Map<String, Object> record = new HashMap<>();
            record.put("title", uploadHistory.getTitle());
            record.put("csv", uploadHistory.getFileName());
            record.put("details", uploadHistory.getDetails());
            record.put("author", uploadHistory.getAuthor());
            record.put("uploadDate", uploadHistory.getUploadDate());
            return record;
        }).collect(Collectors.toList());
    }

    public List<FlightInfo> saveFlightsFromCsv(MultipartFile file, String title, String details, WebRequest request) {
        List<String> flightNames = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            reader.readLine(); // 첫 줄 (헤더) 건너뛰기

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                FlightInfo flight = new FlightInfo();

                flight.setFlightNumber(data[0]);
                flight.setDate(Integer.parseInt(data[1]));
                flight.setPlannedStart(Integer.parseInt(data[2]));
                flight.setPlannedEnd(Integer.parseInt(data[3]));
                flight.setPassengers(Integer.parseInt(data[4]));
                flight.setSeatCost(Double.parseDouble(data[5]));
                flight.setDelayTime(0);
                flight.setCancelled(false); 
                flight.setAdjustedStart(0);
                flight.setAdjustedEnd(0);
                flight.setWindSpeed(Double.parseDouble(data[6]));
                flight.setRainfall(Double.parseDouble(data[7]));
                flight.setVisibility(Double.parseDouble(data[8]));
                flight.setRisk(calculateWeatherRisk(flight));
                flight.setWeather(calculateWeather(flight));
                flightNames.add(flight.getFlightNumber()); // flightNumber를 flightNames 목록에 추가
                flightInfoRepository.save(flight);
            }

//            this.calculateSchedule();

            // UploadHistory에 업로드 기록 저장
            String fileName = file.getOriginalFilename();
            String joinedFlightNames = String.join(", ", flightNames);
            LocalDateTime uploadDate = LocalDateTime.now();
            String authorIp = request.getHeader("X-FORWARDED-FOR"); // 프록시나 로드밸런서 환경에서 클라이언트 IP 가져오기
            if (authorIp == null) {
                authorIp = "none";
            }

            UploadHistory uploadHistory = new UploadHistory(fileName, joinedFlightNames, uploadDate, title, details, authorIp);
            uploadHistoryRepository.save(uploadHistory);
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

        // 필요한 데이터를 Python으로 전달하기 위해 Map에 저장
        Map<String, Object> data = new HashMap<>();
        data.put("n", flights.size());
        data.put("planned_start_times", flights.stream().map(FlightInfo::getPlannedStart).toArray());
        data.put("planned_end_times", flights.stream().map(FlightInfo::getPlannedEnd).toArray());
        data.put("is_delayed", flights.stream().map(FlightInfo::isDelayed).toArray());

        // 날씨 정보 배열 생성
        List<Map<String, Object>> weatherInfoList = new ArrayList<>();
        for (FlightInfo flight : flights) {
            Map<String, Object> weatherInfo = new HashMap<>();
            weatherInfo.put("wind_speed", flight.getWindSpeed());
            weatherInfo.put("rainfall", flight.getRainfall());
            weatherInfo.put("visibility", flight.getVisibility());
            weatherInfoList.add(weatherInfo);
        }
        data.put("weather_info", weatherInfoList);

        data.put("pass_num", flights.stream().map(FlightInfo::getPassengers).toArray());
        data.put("seat_cost", flights.stream().map(FlightInfo::getSeatCost).toArray());
        data.put("b", flights.stream().map(FlightInfo::isInFlight).toArray());
        data.put("lambda_risk", settings.getRiskAlpha());
        data.put("M", 1_000_000);

        try {
            // JSON 형식으로 데이터 직렬화
            ObjectMapper mapper = new ObjectMapper();
            String jsonData = mapper.writeValueAsString(data);

            // Python 스크립트 실행
            ProcessBuilder processBuilder = new ProcessBuilder("python3", "C:\\Users\\84802\\Documents\\gw\\web2\\src\\main\\java\\com\\knu\\cdp1\\service\\flight_scheduler.py"); // Python 스크립트 경로 설정
            Process process = processBuilder.start();

            // Python으로 데이터 전송
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            writer.write(jsonData);
            writer.flush();
            writer.close();

            // Python에서 결과 받아오기
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            // Python 에러 출력 받기
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errorResult = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorResult.append(line).append("\n");
            }

            // 프로세스 종료 후 오류 확인
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Python script failed with error: " + errorResult.toString());
            }

            // JSON 결과 파싱
            List<Map<String, Object>> scheduleResults = mapper.readValue(result.toString(), List.class);
            for (int i = 0; i < flights.size(); i++) {
                FlightInfo flight = flights.get(i);
                Map<String, Object> schedule = scheduleResults.get(i);

                boolean isCancelled = ((Double) schedule.get("cancelled")) == 1.0;
                flight.setCancelled(isCancelled);

                if (!isCancelled) {
                    // 취소되지 않은 경우에만 adjusted 시간을 설정
                    int adjustedStart = ((Number) schedule.get("adjusted_start_time")).intValue();
                    int delayTime = ((Double) schedule.get("delay")).intValue();

                    // isDelayed 값이 0이 아닌 경우 delayTime을 더해 adjustedStart를 업데이트
                    int delay = flight.isDelayed();
                    if (delay > 0) {
                        adjustedStart += delay; // adjusted_start_time에 delay 더함
                    }

                    flight.setAdjustedStart(adjustedStart);
                    flight.setAdjustedEnd(((Number) schedule.get("adjusted_end_time")).intValue());
                }

                flight.setDelayTime(((Double) schedule.get("delay")).intValue());
                flight.setCost((Double) schedule.get("cost"));

                flightInfoRepository.save(flight);
            }



        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to execute Python script: " + e.getMessage());
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

    private String calculateWeather(FlightInfo flight) {
        double windSpeed = flight.getWindSpeed();
        double rainfall = flight.getRainfall();
        double visibility = flight.getVisibility();

        if (rainfall > 0 ){
            return "rain";
        }
        else if (windSpeed > 20){
            return "windy";
        }
        else if (visibility < 5500){
            return "cloudy";
        }else {
            return "sunny";
        }

    }

    private String calculateStatus(FlightInfo flight) {
        // Settings에서 현재 시간을 가져오기
        Settings settings = settingsRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Settings not found"));

        String now = settings.getTime(); // 예: "2024-11-15 12:00:00"

        // FlightInfo에서 출발 및 도착 시간을 가져오기
        String departureTime = calculateFlightTimes(flight).get("adjustedDeparture");
        String arrivalTime = calculateFlightTimes(flight).get("adjustedArrival");

        // String을 LocalDateTime으로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime nowTime = LocalDateTime.parse(now, formatter);
        LocalDateTime departure = LocalDateTime.parse(departureTime, formatter);
        LocalDateTime arrival = LocalDateTime.parse(arrivalTime, formatter);

        // 상태 계산
        if (flight.isCancelled()){
            return "Cancelled";
        }
        else if (nowTime.isAfter(arrival)) {
            return "Completed"; // 현재 시간이 도착 시간 이후
        }
        else if (flight.isDelayed() > 0) {
            return "Delayed"; // 현재 시간이 출발 시간 이전
        }
        else if (nowTime.isBefore(departure)) {
            return "Scheduled"; // 현재 시간이 출발 시간 이전
        } else if (nowTime.isAfter(departure) && nowTime.isBefore(arrival)) {
            return "In Flight"; // 현재 시간이 출발과 도착 시간 사이
        }  else {
            return "Unknown"; // 예외적인 경우
        }
    }


    public static Map<String, String> calculateFlightTimes(FlightInfo flight) {
        Map<String, String> flightMap = new HashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 날짜 정보 가져오기
        int date = flight.getDate();

        // plannedDeparture 계산
        LocalDateTime plannedDeparture = LocalDateTime.of(
                Integer.parseInt(String.valueOf(date).substring(0, 4)),
                Integer.parseInt(String.valueOf(date).substring(4, 6)),
                Integer.parseInt(String.valueOf(date).substring(6, 8)),
                0, 0,0
        ).plusMinutes(flight.getPlannedStart());
        flightMap.put("plannedDeparture", plannedDeparture.format(formatter));

        // plannedArrival 계산
        LocalDateTime plannedArrival = LocalDateTime.of(
                Integer.parseInt(String.valueOf(date).substring(0, 4)),
                Integer.parseInt(String.valueOf(date).substring(4, 6)),
                Integer.parseInt(String.valueOf(date).substring(6, 8)),
                0, 0,0
        ).plusMinutes(flight.getPlannedEnd());
        flightMap.put("plannedArrival", plannedArrival.format(formatter));

        // adjustedDeparture 계산
        LocalDateTime adjustedDeparture = LocalDateTime.of(
                Integer.parseInt(String.valueOf(date).substring(0, 4)),
                Integer.parseInt(String.valueOf(date).substring(4, 6)),
                Integer.parseInt(String.valueOf(date).substring(6, 8)),
                0, 0,0
        ).plusMinutes(flight.getAdjustedStart());
        flightMap.put("adjustedDeparture", adjustedDeparture.format(formatter));

        // adjustedArrival 계산
        LocalDateTime adjustedArrival = LocalDateTime.of(
                Integer.parseInt(String.valueOf(date).substring(0, 4)),
                Integer.parseInt(String.valueOf(date).substring(4, 6)),
                Integer.parseInt(String.valueOf(date).substring(6, 8)),
                0, 0,0
        ).plusMinutes(flight.getAdjustedEnd());
        flightMap.put("adjustedArrival", adjustedArrival.format(formatter));

        return flightMap;
    }

    public List<FlightInfo> calculateFlightPositions() {
        Settings settings = settingsRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Settings not found"));
        List<FlightInfo> flights = flightInfoRepository.findAll();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.parse(settings.getTime(), formatter);

        for (FlightInfo flight: flights) {
            if (flight.isInFlight()) {
                int date = flight.getDate();
                double currentP = flight.getCurrentPosition();
                long elapsedSeconds = java.time.Duration.between(LocalDateTime.parse(flight.getPreviousTime(), formatter), now).getSeconds();

                LocalDateTime adjustedArrival = LocalDateTime.of(
                        Integer.parseInt(String.valueOf(date).substring(0, 4)),
                        Integer.parseInt(String.valueOf(date).substring(4, 6)),
                        Integer.parseInt(String.valueOf(date).substring(6, 8)),
                        0, 0, 0
                ).plusMinutes(flight.getAdjustedEnd());

                double speed = (100 - currentP) / (java.time.Duration.between(now, adjustedArrival).getSeconds());
                currentP += speed * elapsedSeconds;
                flight.setCurrentPosition(currentP);
            }
            flight.setPreviousTime(now.format(formatter));
            flightInfoRepository.save(flight);
        }

        return flights;
    }


}
