package com.jiangsu.guide.repository;

import com.jiangsu.guide.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Favorite> findByUserIdAndSchoolId(Long userId, Long schoolId);

    boolean existsByUserIdAndSchoolId(Long userId, Long schoolId);

    long countByUserId(Long userId);

    void deleteByUserIdAndSchoolId(Long userId, Long schoolId);
}
