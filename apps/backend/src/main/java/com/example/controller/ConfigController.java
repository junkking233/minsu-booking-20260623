package com.example.controller;

import com.example.common.Result;
import com.example.dto.SystemConfigDto;
import com.example.entity.City;
import com.example.entity.Tag;
import com.example.service.ConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class ConfigController {

    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/api/config/cities")
    public Result<List<City>> listCities() {
        return Result.ok(configService.listCities());
    }

    @GetMapping("/api/config/tags")
    public Result<List<Tag>> listTags() {
        return Result.ok(configService.listTags());
    }

    @PostMapping("/api/admin/config/cities")
    public Result<City> addCity(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Integer sort = body.get("sort") != null ? ((Number) body.get("sort")).intValue() : 0;
        return Result.ok("添加成功", configService.addCity(name, sort));
    }

    @DeleteMapping("/api/admin/config/cities/{id}")
    public Result<Void> deleteCity(@PathVariable Long id) {
        configService.deleteCity(id);
        return Result.ok("删除成功", null);
    }

    @PostMapping("/api/admin/config/tags")
    public Result<Tag> addTag(@RequestBody Map<String, String> body) {
        return Result.ok("添加成功", configService.addTag(body.get("name")));
    }

    @DeleteMapping("/api/admin/config/tags/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        configService.deleteTag(id);
        return Result.ok("删除成功", null);
    }

    @GetMapping("/api/admin/config/settings")
    public Result<SystemConfigDto> getSettings() {
        return Result.ok(configService.getSettings());
    }

    @PutMapping("/api/admin/config/settings")
    public Result<Void> saveSettings(@RequestBody SystemConfigDto dto) {
        configService.saveSettings(dto);
        return Result.ok("保存成功", null);
    }
}
