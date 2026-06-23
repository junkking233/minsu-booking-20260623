package com.example.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.Result;
import com.example.dto.HouseSearchQuery;
import com.example.entity.House;
import com.example.service.HouseService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/houses")
public class HouseController {

    private final HouseService houseService;

    public HouseController(HouseService houseService) {
        this.houseService = houseService;
    }

    @GetMapping
    public Result<IPage<House>> search(HouseSearchQuery query) {
        return Result.ok(houseService.search(query));
    }

    @GetMapping("/{id}")
    public Result<House> detail(@PathVariable Long id) {
        return Result.ok(houseService.getById(id));
    }

    @GetMapping("/{id}/availability")
    public Result<Map<String, Object>> availability(@PathVariable Long id,
                                                     @RequestParam String checkIn,
                                                     @RequestParam String checkOut) {
        return Result.ok(houseService.checkAvailability(id, checkIn, checkOut));
    }
}
