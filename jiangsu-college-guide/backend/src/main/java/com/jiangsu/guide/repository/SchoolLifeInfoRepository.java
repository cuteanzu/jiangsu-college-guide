package com.jiangsu.guide.repository;

import com.jiangsu.guide.entity.SchoolLifeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchoolLifeInfoRepository extends JpaRepository<SchoolLifeInfo, Long> {
    Optional<SchoolLifeInfo> findBySchoolId(Long schoolId);
}
