package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.dto.SystemConfigDto;
import com.example.entity.City;
import com.example.entity.SystemConfig;
import com.example.entity.Tag;
import com.example.exception.BusinessException;
import com.example.mapper.CityMapper;
import com.example.mapper.SystemConfigMapper;
import com.example.mapper.TagMapper;
import com.example.service.ConfigService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigServiceImpl implements ConfigService {

    private final CityMapper cityMapper;

    private final TagMapper tagMapper;

    private final SystemConfigMapper systemConfigMapper;

    public ConfigServiceImpl(CityMapper cityMapper, TagMapper tagMapper, SystemConfigMapper systemConfigMapper) {
        this.cityMapper = cityMapper;
        this.tagMapper = tagMapper;
        this.systemConfigMapper = systemConfigMapper;
    }

    @Override
    public List<City> listCities() {
        return cityMapper.selectList(new QueryWrapper<City>().orderByAsc("sort"));
    }

    @Override
    public City addCity(String name, Integer sort) {
        City city = new City();
        city.setName(name);
        city.setSort(sort != null ? sort : 0);
        cityMapper.insert(city);
        return city;
    }

    @Override
    public void deleteCity(Long id) {
        int deleted = cityMapper.deleteById(id);
        if (deleted == 0) {
            throw new BusinessException(404, "城市不存在");
        }
    }

    @Override
    public List<Tag> listTags() {
        return tagMapper.selectList(new QueryWrapper<>());
    }

    @Override
    public Tag addTag(String name) {
        Tag tag = new Tag();
        tag.setName(name);
        tagMapper.insert(tag);
        return tag;
    }

    @Override
    public void deleteTag(Long id) {
        int deleted = tagMapper.deleteById(id);
        if (deleted == 0) {
            throw new BusinessException(404, "标签不存在");
        }
    }

    @Override
    public SystemConfigDto getSettings() {
        SystemConfigDto dto = new SystemConfigDto();
        dto.setDefaultDiscount(getConfigInt("default_discount", 30));
        dto.setPayTimeoutMinutes(getConfigInt("pay_timeout_minutes", 30));
        dto.setFreeCancelHours(getConfigInt("free_cancel_hours", 24));
        dto.setReviewAvailableDays(getConfigInt("review_available_days", 30));
        return dto;
    }

    @Override
    public void saveSettings(SystemConfigDto dto) {
        saveConfigInt("default_discount", dto.getDefaultDiscount());
        saveConfigInt("pay_timeout_minutes", dto.getPayTimeoutMinutes());
        saveConfigInt("free_cancel_hours", dto.getFreeCancelHours());
        saveConfigInt("review_available_days", dto.getReviewAvailableDays());
    }

    private int getConfigInt(String key, int defaultValue) {
        SystemConfig config = systemConfigMapper.selectById(key);
        if (config == null || config.getConfigValue() == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(config.getConfigValue().trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void saveConfigInt(String key, Integer value) {
        if (value == null) return;
        SystemConfig config = new SystemConfig();
        config.setConfigKey(key);
        config.setConfigValue(String.valueOf(value));
        systemConfigMapper.insertOrUpdate(config);
    }
}
