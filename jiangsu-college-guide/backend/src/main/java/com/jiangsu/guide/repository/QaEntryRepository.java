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
    List<QaEntry> findBySchoolIdAndStatus(Long schoolId, String status);
    Optional<QaEntry> findByCode(String code);
    Optional<QaEntry> findByCodeAndStatus(String code, String status);
    Optional<QaEntry> findBySubmissionId(Long submissionId);

    @Query("SELECT q FROM QaEntry q WHERE " +
           "q.status = :status AND " +
           "(:category IS NULL OR q.category = :category) " +
           "ORDER BY q.likeCount DESC")
    List<QaEntry> findByFilters(@Param("category") String category,
                                @Param("status") String status);
}
