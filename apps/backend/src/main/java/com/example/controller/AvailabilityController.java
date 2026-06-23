package com.example.controller;

import com.example.common.Result;
import com.example.dto.CalendarDto;
import com.example.entity.Availability;
import com.example.service.AvailabilityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/houses/{id}/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public Result<CalendarDto> getCalendar(@PathVariable Long id,
                                           @RequestParam String yearMonth) {
        return Result.ok(availabilityService.getCalendar(id, yearMonth));
    }

    @PutMapping("/toggle")
    public Result<Void> toggle(@PathVariable Long id, @RequestBody Map<String, String> body) {
        availabilityService.toggle(id, body.get("date"));
        return Result.ok("操作成功", null);
    }
}
