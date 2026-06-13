package com.jiangsu.guide.repository;

import com.jiangsu.guide.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findBySchoolIdAndStatusOrderByCreatedAtDesc(Long schoolId, String status, Pageable pageable);

    List<Comment> findBySchoolIdAndStatusOrderByCreatedAtDesc(Long schoolId, String status);

    List<Comment> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    List<Comment> findByStatusOrderByCreatedAtDesc(String status);

    long countBySchoolIdAndStatus(Long schoolId, String status);
}
