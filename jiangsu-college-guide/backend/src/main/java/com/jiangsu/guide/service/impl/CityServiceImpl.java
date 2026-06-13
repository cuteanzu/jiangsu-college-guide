package com.jiangsu.guide.service.impl;

import com.jiangsu.guide.dto.HomeStatsDTO;
import com.jiangsu.guide.dto.SchoolDTO;
import com.jiangsu.guide.dto.CityProfileDTO;
import com.jiangsu.guide.entity.City;
import com.jiangsu.guide.entity.School;
import com.jiangsu.guide.exception.BusinessException;
import com.jiangsu.guide.repository.CityRepository;
import com.jiangsu.guide.repository.CommentRepository;
import com.jiangsu.guide.repository.SchoolRepository;
import com.jiangsu.guide.repository.SubmissionRepository;
import com.jiangsu.guide.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;
    private final SchoolRepository schoolRepository;
    private final CommentRepository commentRepository;
    private final SubmissionRepository submissionRepository;

    @Override
    public List<City> getAllCities() {
        return cityRepository.findAllByOrderBySortOrderAsc();
    }

    @Override
    public City getCityById(Long id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "城市不存在"));
    }

    @Override
    public List<SchoolDTO> getSchoolsByCity(Long cityId) {
        List<School> schools = schoolRepository.findByCityId(cityId);
        return schools.stream().map(this::toSchoolDTO).collect(Collectors.toList());
    }

    @Override
    public HomeStatsDTO getHomeStats() {
        return HomeStatsDTO.builder()
                .cityCount(cityRepository.count())
                .schoolCount(schoolRepository.count())
                .commentCount(commentRepository.count())
                .submissionCount(submissionRepository.count())
                .build();
    }

    @Override
    public Map<String, Object> getCityStats(Long cityId) {
        City city = getCityById(cityId);
        long schoolCount = schoolRepository.countByCityId(cityId);
        List<School> hotSchools = schoolRepository.findTop10ByOrderByHotScoreDesc();

        return Map.of(
                "city", city,
                "schoolCount", schoolCount,
                "hotSchools", hotSchools.stream().map(this::toSchoolDTO).collect(Collectors.toList())
        );
    }

    private SchoolDTO toSchoolDTO(School s) {
        return SchoolDTO.builder()
                .id(s.getId())
                .name(s.getName())
                .cityId(s.getCityId())
                .cityName(s.getCity() != null ? s.getCity().getName() : null)
                .type(s.getType())
                .level(s.getLevel())
                .logoUrl(s.getLogoUrl())
                .coverUrl(s.getCoverUrl())
                .website(s.getWebsite())
                .address(s.getAddress())
                .brief(s.getBrief())
                .hotScore(s.getHotScore())
                .favoriteCount(s.getFavoriteCount())
                .mapX(s.getMapX())
                .mapY(s.getMapY())
                .build();
    }

    @Override
    public List<CityProfileDTO> getAllCityProfiles() {
        return cityRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(this::toCityProfileDTO)
                .collect(Collectors.toList());
    }

    private CityProfileDTO toCityProfileDTO(City c) {
        List<String> tagList = c.getTags() != null && !c.getTags().isEmpty()
                ? Arrays.asList(c.getTags().split("\\s*,\\s*"))
                : Collections.emptyList();

        return CityProfileDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .schoolCount(c.getSchoolCount())
                .tags(tagList)
                .cost(c.getCostGuide())
                .transit(c.getTransitGuide())
                .jobs(c.getJobsGuide())
                .audience(c.getAudience())
                .build();
    }
}
