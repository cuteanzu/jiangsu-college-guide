package com.jiangsu.guide.service.impl;

import com.jiangsu.guide.dto.SchoolDTO;
import com.jiangsu.guide.entity.Favorite;
import com.jiangsu.guide.entity.School;
import com.jiangsu.guide.exception.BusinessException;
import com.jiangsu.guide.repository.FavoriteRepository;
import com.jiangsu.guide.repository.SchoolRepository;
import com.jiangsu.guide.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final SchoolRepository schoolRepository;

    @Override
    @Transactional
    public void addFavorite(Long userId, Long schoolId) {
        log.info("▶ 收藏学校: userId={}, schoolId={}", userId, schoolId);
        if (favoriteRepository.existsByUserIdAndSchoolId(userId, schoolId)) {
            log.warn("  已收藏过: userId={}, schoolId={}", userId, schoolId);
            throw new BusinessException("已收藏过该学校");
        }
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new BusinessException(404, "学校不存在"));

        Favorite favorite = Favorite.builder()
                .userId(userId)
                .schoolId(schoolId)
                .build();
        favoriteRepository.save(favorite);

        school.setFavoriteCount(school.getFavoriteCount() + 1);
        schoolRepository.save(school);
        log.info("✔ 收藏成功: {} (收藏数: {})", school.getName(), school.getFavoriteCount());
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long schoolId) {
        log.info("▶ 取消收藏: userId={}, schoolId={}", userId, schoolId);
        favoriteRepository.deleteByUserIdAndSchoolId(userId, schoolId);

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new BusinessException(404, "学校不存在"));
        school.setFavoriteCount(Math.max(0, school.getFavoriteCount() - 1));
        schoolRepository.save(school);
        log.info("✔ 取消收藏成功: {} (收藏数: {})", school.getName(), school.getFavoriteCount());
    }

    @Override
    public List<SchoolDTO> getUserFavorites(Long userId) {
        List<Favorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return favorites.stream()
                .map(f -> {
                    School s = f.getSchool();
                    return SchoolDTO.builder()
                            .id(s.getId())
                            .name(s.getName())
                            .cityId(s.getCityId())
                            .cityName(s.getCity() != null ? s.getCity().getName() : null)
                            .type(s.getType())
                            .level(s.getLevel())
                            .logoUrl(s.getLogoUrl())
                            .brief(s.getBrief())
                            .hotScore(s.getHotScore())
                            .favoriteCount(s.getFavoriteCount())
                            .isFavorited(true)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean isFavorited(Long userId, Long schoolId) {
        return favoriteRepository.existsByUserIdAndSchoolId(userId, schoolId);
    }

    @Override
    public long getFavoriteCount(Long userId) {
        return favoriteRepository.countByUserId(userId);
    }
}
