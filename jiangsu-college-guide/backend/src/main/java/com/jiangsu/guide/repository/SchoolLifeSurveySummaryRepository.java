package com.jiangsu.guide.repository;

import com.jiangsu.guide.entity.SchoolLifeSurveySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchoolLifeSurveySummaryRepository extends JpaRepository<SchoolLifeSurveySummary, Long> {
    Optional<SchoolLifeSurveySummary> findBySchoolId(Long schoolId);
}
