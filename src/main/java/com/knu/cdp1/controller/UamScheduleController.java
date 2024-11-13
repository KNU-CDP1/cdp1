package com.knu.cdp1.controller;

import com.knu.cdp1.model.FlightInfo;
import com.knu.cdp1.service.UamScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UamScheduleController {

    @Autowired
    private UamScheduleService uamScheduleService;

    // 저장된 FlightInfo 데이터를 반환하는 엔드포인트
    @GetMapping("/scheduleHistory")
    public List<Map<String, Object>> getScheduleHistory() {
        List<Map<String, Object>> history = uamScheduleService.getScheduleHistory();
        return history;
    }

    @PostMapping("/Schedule")
    public ResponseEntity<List<FlightInfo>> uploadCsv(@RequestParam("file") MultipartFile file, @RequestParam("details") String details, WebRequest request) {
        List<FlightInfo> flights = uamScheduleService.saveFlightsFromCsv(file, details, request);
        return ResponseEntity.ok(flights);
    }


    @PostMapping("/calculate")
    public List<FlightInfo> calculateSchedule() {
        return uamScheduleService.calculateSchedule();
    }

    @GetMapping("/test")
    public String test() {
        return "UAM Scheduling API is running.";
    }
}
