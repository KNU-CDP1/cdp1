package com.knu.cdp1.controller;

import com.knu.cdp1.model.Settings;
import com.knu.cdp1.repository.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    @Autowired
    private SettingsRepository settingsRepository;

    @GetMapping
    public ResponseEntity<Settings> getSettings() {
        return ResponseEntity.of(settingsRepository.findById(1L));
    }

    @PostMapping
    public ResponseEntity<Settings> updateSettings(@RequestBody Settings newSettings) {
        newSettings.setId(1L); // 항상 하나의 설정만 유지
        settingsRepository.save(newSettings);
        return ResponseEntity.ok(newSettings);
    }
}
