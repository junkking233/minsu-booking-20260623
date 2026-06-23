package com.example.service;

import com.example.dto.CalendarDto;
import com.example.entity.Availability;

import java.util.List;

public interface AvailabilityService {

    CalendarDto getCalendar(Long houseId, String yearMonth);

    void toggle(Long houseId, String date);
}
