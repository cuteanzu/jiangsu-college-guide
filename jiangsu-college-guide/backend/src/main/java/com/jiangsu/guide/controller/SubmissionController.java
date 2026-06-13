package com.jiangsu.guide.controller;

import com.jiangsu.guide.common.Result;
import com.jiangsu.guide.common.SecurityUtils;
import com.jiangsu.guide.dto.SubmissionRequest;
import com.jiangsu.guide.entity.Submission;
import com.jiangsu.guide.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping("/submissions")
    public Result<Submission> createSubmission(@Valid @RequestBody SubmissionRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok("投稿已提交，等待审核", submissionService.createSubmission(userId, request));
    }

    @GetMapping("/user/submissions")
    public Result<List<Submission>> getUserSubmissions() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(submissionService.getUserSubmissions(userId));
    }
}
