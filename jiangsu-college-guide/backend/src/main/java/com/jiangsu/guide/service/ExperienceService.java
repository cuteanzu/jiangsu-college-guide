package com.jiangsu.guide.service;

import com.jiangsu.guide.dto.ExperienceDTO;

import java.util.List;

public interface ExperienceService {
    List<ExperienceDTO> getAllExperiences(String category, String city, Long schoolId);
    ExperienceDTO getByCode(String code);
    List<ExperienceDTO> getBySchoolId(Long schoolId);
}
