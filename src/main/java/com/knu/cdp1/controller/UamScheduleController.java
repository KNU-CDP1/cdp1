package com.knu.cdp1.controller;

import com.knu.cdp1.model.FlightInfo;
import com.knu.cdp1.service.UamScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/schedule")
public class UamScheduleController {

    @Autowired
    private UamScheduleService uamScheduleService;

    // 저장된 FlightInfo 데이터를 반환하는 엔드포인트
//    @GetMapping("/flights")
//    public ResponseEntity<List<FlightInfo>> getAllFlights() {
//        List<FlightInfo> flights = uamScheduleService.getAllFlights();
//        return ResponseEntity.ok(flights);
//    }

    @PostMapping("/upload")
    public ResponseEntity<List<FlightInfo>> uploadCsv(@RequestParam("file") MultipartFile file) {
        List<FlightInfo> flights = uamScheduleService.saveFlightsFromCsv(file);
        return ResponseEntity.ok(flights);
    }


    @PostMapping("/calculate")
    public List<FlightInfo> calculateSchedule(@RequestBody List<FlightInfo> flights) {
        return uamScheduleService.calculateSchedule();
    }

    @GetMapping("/test")
    public String test() {
        return "UAM Scheduling API is running.";
    }
}
