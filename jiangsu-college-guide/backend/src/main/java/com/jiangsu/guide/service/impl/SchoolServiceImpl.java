package com.jiangsu.guide.service.impl;

import com.jiangsu.guide.common.SecurityUtils;
import com.jiangsu.guide.dto.*;
import com.jiangsu.guide.entity.Comment;
import com.jiangsu.guide.entity.School;
import com.jiangsu.guide.entity.SchoolLifeInfo;
import com.jiangsu.guide.exception.BusinessException;
import com.jiangsu.guide.repository.*;
import com.jiangsu.guide.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SchoolServiceImpl implements SchoolService {

    private final SchoolRepository schoolRepository;
    private final SchoolLifeInfoRepository lifeInfoRepository;
    private final CommentRepository commentRepository;
    private final FavoriteRepository favoriteRepository;

    @Override
    public SchoolDetailDTO getSchoolDetail(Long id) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "学校不存在"));

        SchoolLifeInfo lifeInfo = lifeInfoRepository.findBySchoolId(id).orElse(null);
        long commentCount = commentRepository.countBySchoolIdAndStatus(id, "APPROVED");
        boolean isFavorited = false;
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId != null) {
            isFavorited = favoriteRepository.existsByUserIdAndSchoolId(currentUserId, id);
        }

        return SchoolDetailDTO.builder()
                .basic(toSchoolDTO(school, isFavorited))
                .lifeInfo(toLifeInfoDTO(lifeInfo))
                .commentCount(commentCount)
                .isFavorited(isFavorited)
                .build();
    }

    @Override
    @Cacheable(value = "hotSchools", unless = "#result.isEmpty()")
    public List<SchoolDTO> getHotSchools(int limit) {
        return schoolRepository.findTop10ByOrderByHotScoreDesc()
                .stream()
                .limit(limit)
                .map(s -> toSchoolDTO(s, false))
                .collect(Collectors.toList());
    }

    @Override
    public List<SchoolDTO> search(String keyword, Long cityId, String level, String type, int page, int size) {
        return schoolRepository.search(cityId, level, type, keyword, PageRequest.of(page, size))
                .stream()
                .map(s -> toSchoolDTO(s, false))
                .collect(Collectors.toList());
    }

    @Override
    public List<SchoolDTO> getSchoolsByCity(Long cityId) {
        return schoolRepository.findByCityId(cityId)
                .stream()
                .map(s -> toSchoolDTO(s, false))
                .collect(Collectors.toList());
    }

    private SchoolDTO toSchoolDTO(School s, boolean isFavorited) {
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
                .isFavorited(isFavorited)
                .build();
    }

    private LifeInfoDTO toLifeInfoDTO(SchoolLifeInfo info) {
        if (info == null) return null;
        return LifeInfoDTO.builder()
                .dormScore(info.getDormScore())
                .dormDesc(info.getDormDesc())
                .canteenScore(info.getCanteenScore())
                .canteenDesc(info.getCanteenDesc())
                .studyScore(info.getStudyScore())
                .studyDesc(info.getStudyDesc())
                .transportScore(info.getTransportScore())
                .transportDesc(info.getTransportDesc())
                .surroundingScore(info.getSurroundingScore())
                .surroundingDesc(info.getSurroundingDesc())
                .tips(info.getTips())
                .sourceType(info.getSourceType())
                .build();
    }

    @Override
    public List<UniversityDTO> getAllUniversities() {
        return schoolRepository.findAll()
                .stream()
                .map(this::toUniversityDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UniversityDTO getUniversityByCode(String code) {
        School school = schoolRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(404, "学校不存在: " + code));
        return toUniversityDTO(school);
    }

    @Override
    public List<UniversityDTO> getUniversitiesByCity(Long cityId) {
        return schoolRepository.findByCityId(cityId)
                .stream()
                .map(this::toUniversityDTO)
                .collect(Collectors.toList());
    }

    private UniversityDTO toUniversityDTO(School s) {
        return UniversityDTO.builder()
                .id(s.getCode())
                .name(s.getName())
                .city(s.getCity() != null ? s.getCity().getName() : null)
                .lat(s.getLat())
                .lng(s.getLng())
                .tier(s.getLevel())
                .founded(s.getFounded())
                .website(s.getWebsite())
                .build();
    }
}
