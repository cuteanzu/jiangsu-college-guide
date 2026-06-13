package com.jiangsu.guide.controller;

import com.jiangsu.guide.common.Result;
import com.jiangsu.guide.dto.HomeStatsDTO;
import com.jiangsu.guide.dto.SchoolDTO;
import com.jiangsu.guide.dto.CityProfileDTO;
import com.jiangsu.guide.entity.City;
import com.jiangsu.guide.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @GetMapping
    public Result<List<City>> listCities() {
        return Result.ok(cityService.getAllCities());
    }

    @GetMapping("/{id}")
    public Result<City> getCity(@PathVariable Long id) {
        return Result.ok(cityService.getCityById(id));
    }

    @GetMapping("/{id}/schools")
    public Result<List<SchoolDTO>> getCitySchools(@PathVariable Long id) {
        return Result.ok(cityService.getSchoolsByCity(id));
    }

    @GetMapping("/stats")
    public Result<HomeStatsDTO> getStats() {
        return Result.ok(cityService.getHomeStats());
    }

    @GetMapping("/{id}/detail-stats")
    public Result<Map<String, Object>> getCityStats(@PathVariable Long id) {
        return Result.ok(cityService.getCityStats(id));
    }

    @GetMapping("/profiles")
    public Result<List<CityProfileDTO>> getCityProfiles() {
        return Result.ok(cityService.getAllCityProfiles());
    }
}
