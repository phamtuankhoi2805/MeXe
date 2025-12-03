package com.example.asmproject.service;

import com.example.asmproject.model.Configuration;
import com.example.asmproject.repository.ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConfigurationService {

    @Autowired
    private ConfigurationRepository configurationRepository;

    public String getValue(String key) {
        return configurationRepository.findByKey(key)
                .map(Configuration::getValue)
                .orElse(null);
    }

    public void saveValue(String key, String value) {
        Configuration config = configurationRepository.findByKey(key)
                .orElse(new Configuration());
        if (config.getKey() == null) {
            config.setKey(key);
        }
        config.setValue(value);
        configurationRepository.save(config);
    }

    public Map<String, String> getAllConfigs() {
        return configurationRepository.findAll().stream()
                .collect(Collectors.toMap(Configuration::getKey,
                        c -> c.getValue() != null ? c.getValue() : ""));
    }

    public void saveAll(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            saveValue(entry.getKey(), entry.getValue());
        }
    }
}
