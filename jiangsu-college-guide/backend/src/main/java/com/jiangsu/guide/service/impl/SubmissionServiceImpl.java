package com.jiangsu.guide.service.impl;

import com.jiangsu.guide.dto.SubmissionRequest;
import com.jiangsu.guide.entity.Submission;
import com.jiangsu.guide.exception.BusinessException;
import com.jiangsu.guide.repository.SubmissionRepository;
import com.jiangsu.guide.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;

    @Override
    public Submission createSubmission(Long userId, SubmissionRequest request) {
        log.info("▶ 提交投稿: userId={}, type={}, title={}, schoolId={}",
                userId, request.getType(), request.getTitle(), request.getSchoolId());
        Submission submission = Submission.builder()
                .userId(userId)
                .schoolId(request.getSchoolId())
                .schoolName(request.getSchoolName())
                .type(request.getType())
                .title(request.getTitle())
                .content(request.getContent())
                .isAnonymous(request.isAnonymous() ? 1 : 0)
                .contact(request.getContact())
                .status("PENDING")
                .build();

        Submission saved = submissionRepository.save(submission);
        log.info("✔ 投稿提交成功: submissionId={}", saved.getId());
        return saved;
    }

    @Override
    public List<Submission> getUserSubmissions(Long userId) {
        return submissionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Submission> getPendingSubmissions(int page, int size) {
        return submissionRepository.findByStatusOrderByCreatedAtDesc("PENDING");
    }

    @Override
    public void approveSubmission(Long submissionId, Long reviewerId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new BusinessException(404, "投稿不存在"));
        if (!"PENDING".equals(submission.getStatus())) {
            throw new BusinessException("投稿状态不是待审核");
        }
        submission.setStatus("APPROVED");
        submission.setReviewerId(reviewerId);
        submission.setReviewedAt(LocalDateTime.now());
        submissionRepository.save(submission);
    }

    @Override
    public void rejectSubmission(Long submissionId, Long reviewerId, String reason) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new BusinessException(404, "投稿不存在"));
        if (!"PENDING".equals(submission.getStatus())) {
            throw new BusinessException("投稿状态不是待审核");
        }
        submission.setStatus("REJECTED");
        submission.setReviewerId(reviewerId);
        submission.setRejectReason(reason);
        submission.setReviewedAt(LocalDateTime.now());
        submissionRepository.save(submission);
    }

    @Override
    public long getPendingCount() {
        return submissionRepository.countByStatus("PENDING");
    }
}
