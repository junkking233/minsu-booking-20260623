package com.example.service;

import com.example.dto.SystemConfigDto;
import com.example.entity.City;
import com.example.entity.Tag;

import java.util.List;

public interface ConfigService {

    List<City> listCities();

    City addCity(String name, Integer sort);

    void deleteCity(Long id);

    List<Tag> listTags();

    Tag addTag(String name);

    void deleteTag(Long id);

    SystemConfigDto getSettings();

    void saveSettings(SystemConfigDto dto);
}
