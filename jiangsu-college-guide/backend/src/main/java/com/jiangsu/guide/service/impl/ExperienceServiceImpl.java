package com.jiangsu.guide.service.impl;

import com.jiangsu.guide.dto.ExperienceDTO;
import com.jiangsu.guide.entity.Experience;
import com.jiangsu.guide.entity.School;
import com.jiangsu.guide.exception.BusinessException;
import com.jiangsu.guide.repository.ExperienceRepository;
import com.jiangsu.guide.repository.SchoolRepository;
import com.jiangsu.guide.service.ExperienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExperienceServiceImpl implements ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final SchoolRepository schoolRepository;

    @Override
    public List<ExperienceDTO> getAllExperiences(String category, String city, Long schoolId) {
        return experienceRepository.findByFilters(category, city, schoolId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ExperienceDTO getByCode(String code) {
        Experience exp = experienceRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(404, "经验文章不存在"));
        return toDTO(exp);
    }

    @Override
    public List<ExperienceDTO> getBySchoolId(Long schoolId) {
        return experienceRepository.findBySchoolId(schoolId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private ExperienceDTO toDTO(Experience e) {
        String schoolCode = null;
        if (e.getSchoolId() != null) {
            schoolCode = schoolRepository.findById(e.getSchoolId())
                    .map(School::getCode)
                    .orElse(null);
        }

        List<String> tagList = e.getTags() != null && !e.getTags().isEmpty()
                ? Arrays.asList(e.getTags().split("\\s*,\\s*"))
                : Collections.emptyList();

        return ExperienceDTO.builder()
                .id(e.getCode())
                .category(e.getCategory())
                .schoolId(schoolCode)
                .schoolName(e.getSchoolName())
                .city(e.getCity())
                .title(e.getTitle())
                .excerpt(e.getExcerpt())
                .body(e.getBody())
                .likes(e.getLikeCount())
                .comments(e.getCommentCount())
                .tags(tagList)
                .build();
    }
}
