package com.jiangsu.guide.service;

import com.jiangsu.guide.entity.City;
import com.jiangsu.guide.dto.HomeStatsDTO;
import com.jiangsu.guide.dto.SchoolDTO;
import com.jiangsu.guide.dto.CityProfileDTO;

import java.util.List;
import java.util.Map;

public interface CityService {
    List<City> getAllCities();
    City getCityById(Long id);
    List<SchoolDTO> getSchoolsByCity(Long cityId);
    HomeStatsDTO getHomeStats();
    Map<String, Object> getCityStats(Long cityId);
    List<CityProfileDTO> getAllCityProfiles();
}
