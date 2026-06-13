package com.jiangsu.guide.service;

import com.jiangsu.guide.dto.SubmissionRequest;
import com.jiangsu.guide.entity.Submission;

import java.util.List;

public interface SubmissionService {
    Submission createSubmission(Long userId, SubmissionRequest request);
    List<Submission> getUserSubmissions(Long userId);
    List<Submission> getPendingSubmissions(int page, int size);
    void approveSubmission(Long submissionId, Long reviewerId);
    void rejectSubmission(Long submissionId, Long reviewerId, String reason);
    long getPendingCount();
}
