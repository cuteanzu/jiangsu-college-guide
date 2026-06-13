package com.jiangsu.guide.repository;

import com.jiangsu.guide.entity.Submission;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Submission> findByStatusOrderByCreatedAtDesc(String status);

    List<Submission> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(String status);
}
