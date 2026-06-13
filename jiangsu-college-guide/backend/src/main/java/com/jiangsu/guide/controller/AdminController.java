package com.jiangsu.guide.controller;

import com.jiangsu.guide.common.Result;
import com.jiangsu.guide.common.SecurityUtils;
import com.jiangsu.guide.entity.*;
import com.jiangsu.guide.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final CityRepository cityRepository;
    private final CommentRepository commentRepository;
    private final SubmissionRepository submissionRepository;
    private final ExperienceRepository experienceRepository;
    private final QaEntryRepository qaEntryRepository;
    private final SchoolLifeInfoRepository schoolLifeInfoRepository;
    private final PasswordEncoder passwordEncoder;

    // ========== Dashboard ==========
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userCount", userRepository.count());
        data.put("schoolCount", schoolRepository.count());
        data.put("cityCount", cityRepository.count());
        data.put("commentCount", commentRepository.count());
        data.put("pendingSubmissionCount", submissionRepository.countByStatus("PENDING"));
        data.put("pendingCommentCount", commentRepository.findByStatusOrderByCreatedAtDesc("PENDING", PageRequest.of(0, 100)).size());
        return Result.ok(data);
    }

    // ========== 用户管理 ==========
    @GetMapping("/users")
    public Result<List<User>> listUsers() {
        List<User> users = userRepository.findAll();
        users.forEach(u -> u.setPassword(null));
        return Result.ok(users);
    }

    // ========== 学校管理 ==========
    @GetMapping("/schools")
    public Result<List<School>> listSchools() {
        return Result.ok(schoolRepository.findAll());
    }

    @PostMapping("/schools")
    public Result<School> createSchool(@RequestBody School school) {
        return Result.ok(schoolRepository.save(school));
    }

    @PutMapping("/schools/{id}")
    public Result<School> updateSchool(@PathVariable Long id, @RequestBody School school) {
        school.setId(id);
        return Result.ok(schoolRepository.save(school));
    }

    @DeleteMapping("/schools/{id}")
    public Result<Void> deleteSchool(@PathVariable Long id) {
        schoolRepository.deleteById(id);
        return Result.ok(null);
    }

    // ========== 评论审核 ==========
    @GetMapping("/comments")
    public Result<List<Comment>> listComments(@RequestParam(defaultValue = "PENDING") String status) {
        return Result.ok(commentRepository.findByStatusOrderByCreatedAtDesc(status));
    }

    @PostMapping("/comments/{id}/approve")
    public Result<Void> approveComment(@PathVariable Long id) {
        Comment comment = commentRepository.findById(id).orElseThrow();
        comment.setStatus("APPROVED");
        commentRepository.save(comment);
        return Result.ok(null);
    }

    @PostMapping("/comments/{id}/reject")
    public Result<Void> rejectComment(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Comment comment = commentRepository.findById(id).orElseThrow();
        comment.setStatus("REJECTED");
        comment.setRejectReason(body.get("reason"));
        commentRepository.save(comment);
        return Result.ok(null);
    }

    // ========== 投稿审核 ==========
    @GetMapping("/submissions")
    public Result<List<Submission>> listSubmissions(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return Result.ok(submissionRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)));
    }

    @PostMapping("/submissions/{id}/approve")
    public Result<Void> approveSubmission(@PathVariable Long id) {
        Submission submission = submissionRepository.findById(id).orElseThrow();
        submission.setStatus("APPROVED");
        submission.setReviewerId(SecurityUtils.getCurrentUserId());
        submissionRepository.save(submission);
        return Result.ok(null);
    }

    @PostMapping("/submissions/{id}/reject")
    public Result<Void> rejectSubmission(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Submission submission = submissionRepository.findById(id).orElseThrow();
        submission.setStatus("REJECTED");
        submission.setRejectReason(body.get("reason"));
        submission.setReviewerId(SecurityUtils.getCurrentUserId());
        submissionRepository.save(submission);
        return Result.ok(null);
    }

    // ========== 城市管理 ==========
    @PostMapping("/cities")
    public Result<City> createCity(@RequestBody City city) {
        return Result.ok(cityRepository.save(city));
    }

    @PutMapping("/cities/{id}")
    public Result<City> updateCity(@PathVariable Long id, @RequestBody City city) {
        city.setId(id);
        return Result.ok(cityRepository.save(city));
    }

    // ========== 校园经验管理 ==========
    @GetMapping("/experiences")
    public Result<List<Experience>> listExperiences() {
        return Result.ok(experienceRepository.findAll());
    }

    @PostMapping("/experiences")
    public Result<Experience> createExperience(@RequestBody Experience experience) {
        if (experience.getCode() == null || experience.getCode().isBlank()) {
            experience.setCode("exp-" + System.currentTimeMillis());
        }
        return Result.ok(experienceRepository.save(experience));
    }

    @PutMapping("/experiences/{id}")
    public Result<Experience> updateExperience(@PathVariable Long id, @RequestBody Experience experience) {
        experience.setId(id);
        return Result.ok(experienceRepository.save(experience));
    }

    @DeleteMapping("/experiences/{id}")
    public Result<Void> deleteExperience(@PathVariable Long id) {
        experienceRepository.deleteById(id);
        return Result.ok(null);
    }

    // ========== 问答管理 ==========
    @GetMapping("/qa")
    public Result<List<QaEntry>> listQA() {
        return Result.ok(qaEntryRepository.findAll());
    }

    @PostMapping("/qa")
    public Result<QaEntry> createQA(@RequestBody QaEntry qaEntry) {
        if (qaEntry.getCode() == null || qaEntry.getCode().isBlank()) {
            qaEntry.setCode("qa-" + System.currentTimeMillis());
        }
        return Result.ok(qaEntryRepository.save(qaEntry));
    }

    @PutMapping("/qa/{id}")
    public Result<QaEntry> updateQA(@PathVariable Long id, @RequestBody QaEntry qaEntry) {
        qaEntry.setId(id);
        return Result.ok(qaEntryRepository.save(qaEntry));
    }

    @DeleteMapping("/qa/{id}")
    public Result<Void> deleteQA(@PathVariable Long id) {
        qaEntryRepository.deleteById(id);
        return Result.ok(null);
    }

    // ========== 学校生活信息管理 ==========
    @GetMapping("/life-info/{schoolId}")
    public Result<SchoolLifeInfo> getLifeInfo(@PathVariable Long schoolId) {
        return Result.ok(schoolLifeInfoRepository.findBySchoolId(schoolId).orElse(null));
    }

    @PostMapping("/life-info")
    public Result<SchoolLifeInfo> createLifeInfo(@RequestBody SchoolLifeInfo lifeInfo) {
        // 如果已有则更新
        SchoolLifeInfo existing = schoolLifeInfoRepository.findBySchoolId(lifeInfo.getSchoolId()).orElse(null);
        if (existing != null) {
            lifeInfo.setId(existing.getId());
        }
        lifeInfo.setAuditStatus("APPROVED");
        return Result.ok(schoolLifeInfoRepository.save(lifeInfo));
    }

    @PutMapping("/life-info/{id}")
    public Result<SchoolLifeInfo> updateLifeInfo(@PathVariable Long id, @RequestBody SchoolLifeInfo lifeInfo) {
        lifeInfo.setId(id);
        return Result.ok(schoolLifeInfoRepository.save(lifeInfo));
    }

    @DeleteMapping("/life-info/{id}")
    public Result<Void> deleteLifeInfo(@PathVariable Long id) {
        schoolLifeInfoRepository.deleteById(id);
        return Result.ok(null);
    }
}
