package com.jiangsu.guide.service;

import com.jiangsu.guide.dto.SchoolDTO;

import java.util.List;

public interface FavoriteService {
    void addFavorite(Long userId, Long schoolId);
    void removeFavorite(Long userId, Long schoolId);
    List<SchoolDTO> getUserFavorites(Long userId);
    boolean isFavorited(Long userId, Long schoolId);
    long getFavoriteCount(Long userId);
}
