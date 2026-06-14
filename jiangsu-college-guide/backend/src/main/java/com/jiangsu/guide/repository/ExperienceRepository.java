package com.jiangsu.guide.repository;

import com.jiangsu.guide.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    List<Experience> findByCategory(String category);
    List<Experience> findBySchoolId(Long schoolId);
    List<Experience> findBySchoolIdAndStatus(Long schoolId, String status);
    List<Experience> findByCity(String city);
    Optional<Experience> findByCode(String code);
    Optional<Experience> findByCodeAndStatus(String code, String status);
    Optional<Experience> findBySubmissionId(Long submissionId);

    @Query("SELECT e FROM Experience e WHERE " +
           "e.status = :status AND " +
           "(:category IS NULL OR e.category = :category) AND " +
           "(:city IS NULL OR e.city = :city) AND " +
           "(:schoolId IS NULL OR e.schoolId = :schoolId) " +
           "ORDER BY e.likeCount DESC")
    List<Experience> findByFilters(@Param("category") String category,
                                    @Param("city") String city,
                                    @Param("schoolId") Long schoolId,
                                    @Param("status") String status);
}
