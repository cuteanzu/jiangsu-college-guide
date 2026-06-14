package com.jiangsu.guide.repository;

import com.jiangsu.guide.entity.UserLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLikeRepository extends JpaRepository<UserLike, Long> {

    /**
     * 检查用户是否已对某目标点赞
     */
    boolean existsByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

    /**
     * 统计某目标的点赞总数
     */
    long countByTargetTypeAndTargetId(String targetType, Long targetId);

    /**
     * 取消点赞
     */
    void deleteByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
}
