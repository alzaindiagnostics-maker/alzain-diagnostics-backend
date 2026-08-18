package com.alzain.service;

import com.alzain.entity.BusinessSetting;
import com.alzain.repository.BusinessSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BusinessSettingService {

    @Autowired
    private BusinessSettingRepository businessSettingRepository;

    public Map<String, String> getAllSettingsAsMap() {
        List<BusinessSetting> settings = businessSettingRepository.findAll();
        Map<String, String> map = new HashMap<>();
        for (BusinessSetting setting : settings) {
            map.put(setting.getSettingKey(), setting.getSettingValue());
        }
        return map;
    }
}
