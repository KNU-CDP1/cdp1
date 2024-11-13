package com.knu.cdp1.service;

import com.knu.cdp1.model.Settings;
import com.knu.cdp1.repository.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    @Autowired
    private SettingsRepository settingsRepository;


}
