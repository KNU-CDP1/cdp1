package com.knu.cdp1.service;

import com.knu.cdp1.model.Settings;
import com.knu.cdp1.repository.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    @Autowired
    private SettingsRepository settingsRepository;

    /**
     * 설정 값을 조회합니다. 설정 값이 없으면 기본값으로 초기화하여 저장 후 반환합니다.
     */
    public Settings getSettings() {
        return settingsRepository.findById(1L).orElseGet(() -> {
            Settings defaultSettings = new Settings();
            defaultSettings.setId(1L);
            defaultSettings.setDelayPenalty(50.0);
            defaultSettings.setCancelPenalty(500.0);
            defaultSettings.setWeatherRiskWeight(100000.0);
            return settingsRepository.save(defaultSettings);
        });
    }

    /**
     * 설정 값을 업데이트합니다. 설정 값은 하나의 레코드로 유지됩니다.
     */
    public Settings updateSettings(Settings newSettings) {
        newSettings.setId(1L); // 항상 하나의 설정만 유지
        return settingsRepository.save(newSettings);
    }
}
