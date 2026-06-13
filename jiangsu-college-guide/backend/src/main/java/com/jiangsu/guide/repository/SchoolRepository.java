package com.jiangsu.guide.repository;

import com.jiangsu.guide.entity.School;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {

    List<School> findByCityId(Long cityId);

    List<School> findByLevel(String level);

    List<School> findTop10ByOrderByHotScoreDesc();

    List<School> findByNameContaining(String keyword);

    @Query("SELECT s FROM School s WHERE " +
           "(:cityId IS NULL OR s.cityId = :cityId) AND " +
           "(:level IS NULL OR s.level = :level) AND " +
           "(:type IS NULL OR s.type = :type) AND " +
           "(:keyword IS NULL OR s.name LIKE %:keyword%) " +
           "ORDER BY s.hotScore DESC")
    List<School> search(@Param("cityId") Long cityId,
                        @Param("level") String level,
                        @Param("type") String type,
                        @Param("keyword") String keyword,
                        Pageable pageable);

    long countByCityId(Long cityId);

    Optional<School> findByCode(String code);
}
