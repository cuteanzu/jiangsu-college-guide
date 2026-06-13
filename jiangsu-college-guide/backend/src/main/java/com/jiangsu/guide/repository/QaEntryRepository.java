package com.jiangsu.guide.repository;

import com.jiangsu.guide.entity.QaEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QaEntryRepository extends JpaRepository<QaEntry, Long> {
    List<QaEntry> findByCategory(String category);
    List<QaEntry> findBySchoolId(Long schoolId);
    Optional<QaEntry> findByCode(String code);

    @Query("SELECT q FROM QaEntry q WHERE " +
           "(:category IS NULL OR q.category = :category) " +
           "ORDER BY q.likeCount DESC")
    List<QaEntry> findByFilters(@Param("category") String category);
}
