package com.jiangsu.guide.service.impl;

import com.jiangsu.guide.dto.SubmissionRequest;
import com.jiangsu.guide.entity.Comment;
import com.jiangsu.guide.entity.Experience;
import com.jiangsu.guide.entity.QaEntry;
import com.jiangsu.guide.entity.School;
import com.jiangsu.guide.entity.SchoolLifeInfo;
import com.jiangsu.guide.entity.Submission;
import com.jiangsu.guide.exception.BusinessException;
import com.jiangsu.guide.repository.CommentRepository;
import com.jiangsu.guide.repository.ExperienceRepository;
import com.jiangsu.guide.repository.QaEntryRepository;
import com.jiangsu.guide.repository.SchoolLifeInfoRepository;
import com.jiangsu.guide.repository.SchoolRepository;
import com.jiangsu.guide.repository.SubmissionRepository;
import com.jiangsu.guide.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String SOURCE_USER_SUBMIT = "USER_SUBMIT";

    private final SubmissionRepository submissionRepository;
    private final ExperienceRepository experienceRepository;
    private final QaEntryRepository qaEntryRepository;
    private final SchoolLifeInfoRepository schoolLifeInfoRepository;
    private final CommentRepository commentRepository;
    private final SchoolRepository schoolRepository;

    @Override
    @Transactional
    public Submission createSubmission(Long userId, SubmissionRequest request) {
        log.info("Create submission: userId={}, type={}, title={}, schoolId={}",
                userId, request.getType(), request.getTitle(), request.getSchoolId());

        Submission submission = Submission.builder()
                .userId(userId)
                .schoolId(request.getSchoolId())
                .schoolName(request.getSchoolName())
                .type(normalizeType(request.getType()))
                .title(clean(request.getTitle()))
                .content(requiredContent(request.getContent()))
                .isAnonymous(request.isAnonymous() ? 1 : 0)
                .contact(clean(request.getContact()))
                .status(STATUS_PENDING)
                .build();

        Submission saved = submissionRepository.save(submission);
        log.info("Submission created: submissionId={}", saved.getId());
        return saved;
    }

    @Override
    public List<Submission> getUserSubmissions(Long userId) {
        return submissionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Submission> getPendingSubmissions(int page, int size) {
        return submissionRepository.findByStatusOrderByCreatedAtDesc(STATUS_PENDING);
    }

    @Override
    @Transactional
    public void approveSubmission(Long submissionId, Long reviewerId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new BusinessException(404, "投稿不存在"));
        if (!STATUS_PENDING.equals(submission.getStatus())) {
            throw new BusinessException("投稿状态不是待审核");
        }

        if (isBlank(submission.getConvertedType())) {
            ConversionResult result = convertApprovedSubmission(submission);
            submission.setConvertedType(result.type());
            submission.setConvertedId(result.id());
        }

        submission.setStatus(STATUS_APPROVED);
        submission.setReviewerId(reviewerId);
        submission.setReviewedAt(LocalDateTime.now());
        submissionRepository.save(submission);
    }

    @Override
    @Transactional
    public void rejectSubmission(Long submissionId, Long reviewerId, String reason) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new BusinessException(404, "投稿不存在"));
        if (!STATUS_PENDING.equals(submission.getStatus())) {
            throw new BusinessException("投稿状态不是待审核");
        }
        submission.setStatus(STATUS_REJECTED);
        submission.setReviewerId(reviewerId);
        submission.setRejectReason(clean(reason));
        submission.setReviewedAt(LocalDateTime.now());
        submissionRepository.save(submission);
    }

    @Override
    public long getPendingCount() {
        return submissionRepository.countByStatus(STATUS_PENDING);
    }

    private ConversionResult convertApprovedSubmission(Submission submission) {
        String type = normalizeType(submission.getType());
        Optional<School> school = resolveSchool(submission);

        return switch (type) {
            case "EXPERIENCE" -> convertToExperience(submission, school);
            case "QUESTION", "QA" -> convertToQa(submission, school);
            case "CORRECTION" -> convertToComment(submission, school);
            case "NEW_INFO", "LIFE_INFO", "DORM", "CANTEEN", "TRANSPORT", "STUDY", "SURROUNDING" ->
                    convertToLifeInfo(submission, school);
            case "SUGGESTION" -> ConversionResult.internal();
            default -> {
                log.info("Submission {} approved without public conversion, unknown type={}", submission.getId(), type);
                yield ConversionResult.internal();
            }
        };
    }

    private ConversionResult convertToExperience(Submission submission, Optional<School> school) {
        Optional<Experience> existing = experienceRepository.findBySubmissionId(submission.getId());
        if (existing.isPresent()) {
            return new ConversionResult("EXPERIENCE", existing.get().getId());
        }

        Experience experience = Experience.builder()
                .code("user-exp-" + submission.getId())
                .category(inferExperienceCategory(submission))
                .schoolId(resolveSchoolId(submission, school))
                .schoolName(resolveSchoolName(submission, school))
                .city(resolveCityName(school))
                .title(limit(defaultTitle(submission, "校园经验"), 200))
                .excerpt(limit(summary(submission.getContent(), 160), 500))
                .body(requiredContent(submission.getContent()))
                .likeCount(0)
                .commentCount(0)
                .tags("用户投稿")
                .sourceType(SOURCE_USER_SUBMIT)
                .submissionId(submission.getId())
                .status(STATUS_APPROVED)
                .build();

        Experience saved = experienceRepository.save(experience);
        return new ConversionResult("EXPERIENCE", saved.getId());
    }

    private ConversionResult convertToQa(Submission submission, Optional<School> school) {
        Optional<QaEntry> existing = qaEntryRepository.findBySubmissionId(submission.getId());
        if (existing.isPresent()) {
            return new ConversionResult("QA", existing.get().getId());
        }

        String title = clean(submission.getTitle());
        String content = requiredContent(submission.getContent());
        String question = !isBlank(title) ? title : firstLine(content, 180);
        String answer = !isBlank(title) && !title.equals(content)
                ? content
                : "这个问题已收录，正在等待更多同学补充回答。";

        QaEntry qaEntry = QaEntry.builder()
                .code("user-qa-" + submission.getId())
                .question(question)
                .answer(answer)
                .schoolId(resolveSchoolId(submission, school))
                .schoolName(resolveSchoolName(submission, school))
                .category(inferQaCategory(submission))
                .likeCount(0)
                .sourceType(SOURCE_USER_SUBMIT)
                .submissionId(submission.getId())
                .status(STATUS_APPROVED)
                .build();

        QaEntry saved = qaEntryRepository.save(qaEntry);
        return new ConversionResult("QA", saved.getId());
    }

    private ConversionResult convertToComment(Submission submission, Optional<School> school) {
        Long schoolId = resolveSchoolId(submission, school);
        if (schoolId == null) {
            return ConversionResult.internal();
        }

        Comment comment = Comment.builder()
                .schoolId(schoolId)
                .userId(submission.getUserId())
                .content(requiredContent(submission.getContent()))
                .category("CORRECTION")
                .isAnonymous(submission.getIsAnonymous())
                .likeCount(0)
                .sourceType(SOURCE_USER_SUBMIT)
                .submissionId(submission.getId())
                .status(STATUS_APPROVED)
                .build();

        Comment saved = commentRepository.save(comment);
        return new ConversionResult("COMMENT", saved.getId());
    }

    private ConversionResult convertToLifeInfo(Submission submission, Optional<School> school) {
        Long schoolId = resolveSchoolId(submission, school);
        if (schoolId == null) {
            return ConversionResult.internal();
        }

        SchoolLifeInfo lifeInfo = schoolLifeInfoRepository.findBySchoolId(schoolId)
                .orElseGet(() -> SchoolLifeInfo.builder().schoolId(schoolId).build());

        String note = note(submission);
        switch (normalizeType(submission.getType())) {
            case "DORM" -> lifeInfo.setDormDesc(appendNote(lifeInfo.getDormDesc(), note));
            case "CANTEEN" -> lifeInfo.setCanteenDesc(appendNote(lifeInfo.getCanteenDesc(), note));
            case "STUDY" -> lifeInfo.setStudyDesc(appendNote(lifeInfo.getStudyDesc(), note));
            case "TRANSPORT" -> lifeInfo.setTransportDesc(appendNote(lifeInfo.getTransportDesc(), note));
            case "SURROUNDING" -> lifeInfo.setSurroundingDesc(appendNote(lifeInfo.getSurroundingDesc(), note));
            default -> lifeInfo.setTips(appendNote(lifeInfo.getTips(), note));
        }

        lifeInfo.setSourceType(SOURCE_USER_SUBMIT);
        lifeInfo.setSubmissionId(submission.getId());
        lifeInfo.setAuditStatus(STATUS_APPROVED);

        SchoolLifeInfo saved = schoolLifeInfoRepository.save(lifeInfo);
        return new ConversionResult("LIFE_INFO", saved.getId());
    }

    private Optional<School> resolveSchool(Submission submission) {
        if (submission.getSchoolId() != null) {
            Optional<School> school = schoolRepository.findById(submission.getSchoolId());
            if (school.isPresent()) {
                return school;
            }
        }

        String schoolName = clean(submission.getSchoolName());
        if (isBlank(schoolName)) {
            return Optional.empty();
        }

        List<School> candidates = schoolRepository.findByNameContaining(schoolName);
        return candidates.stream()
                .filter(s -> schoolName.equals(s.getName()))
                .findFirst()
                .or(() -> candidates.stream().findFirst());
    }

    private Long resolveSchoolId(Submission submission, Optional<School> school) {
        return school.map(School::getId).orElse(submission.getSchoolId());
    }

    private String resolveSchoolName(Submission submission, Optional<School> school) {
        return school.map(School::getName).orElse(clean(submission.getSchoolName()));
    }

    private String resolveCityName(Optional<School> school) {
        return school.map(School::getCity)
                .map(city -> clean(city.getName()))
                .orElse(null);
    }

    private String inferExperienceCategory(Submission submission) {
        String text = searchableText(submission);
        if (text.contains("宿舍") || text.contains("dorm")) {
            return "dorm";
        }
        if (text.contains("食堂") || text.contains("吃") || text.contains("canteen")) {
            return "cafeteria";
        }
        if (text.contains("考研") || text.contains("考试") || text.contains("exam")) {
            return "exam";
        }
        if (text.contains("就业") || text.contains("实习") || text.contains("career")) {
            return "career";
        }
        if (text.contains("学习") || text.contains("图书馆") || text.contains("study")) {
            return "study";
        }
        if (text.contains("城市") || text.contains("交通") || text.contains("周边") || text.contains("city")) {
            return "city-life";
        }
        return "freshman";
    }

    private String inferQaCategory(Submission submission) {
        String type = normalizeType(submission.getType());
        if ("DORM".equals(type)) {
            return "dorm";
        }
        if ("CANTEEN".equals(type)) {
            return "cafeteria";
        }
        if ("TRANSPORT".equals(type) || "SURROUNDING".equals(type)) {
            return "life";
        }
        if ("STUDY".equals(type)) {
            return "study";
        }
        return inferExperienceCategory(submission);
    }

    private String searchableText(Submission submission) {
        return (clean(submission.getType()) + " " + clean(submission.getTitle()) + " " + clean(submission.getContent()))
                .toLowerCase(Locale.ROOT);
    }

    private String defaultTitle(Submission submission, String fallback) {
        String title = clean(submission.getTitle());
        return !isBlank(title) ? title : firstLine(submission.getContent(), 80, fallback);
    }

    private String note(Submission submission) {
        String title = clean(submission.getTitle());
        String content = requiredContent(submission.getContent());
        return isBlank(title) ? content : title + "\n" + content;
    }

    private String appendNote(String existing, String addition) {
        if (isBlank(existing)) {
            return addition;
        }
        if (existing.contains(addition)) {
            return existing;
        }
        return existing + "\n\n" + addition;
    }

    private String summary(String content, int maxLength) {
        return firstLine(content, maxLength, "用户提交的校园线索");
    }

    private String firstLine(String text, int maxLength) {
        return firstLine(text, maxLength, "");
    }

    private String firstLine(String text, int maxLength, String fallback) {
        String cleaned = clean(text);
        if (isBlank(cleaned)) {
            return fallback;
        }
        String first = cleaned.split("\\R", 2)[0].trim();
        return limit(first, maxLength);
    }

    private String requiredContent(String content) {
        String cleaned = clean(content);
        if (isBlank(cleaned)) {
            throw new BusinessException("投稿内容不能为空");
        }
        return cleaned;
    }

    private String normalizeType(String type) {
        if (isBlank(type)) {
            return "SUGGESTION";
        }
        return type.trim().toUpperCase(Locale.ROOT).replace('-', '_');
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 1)) + "...";
    }

    private record ConversionResult(String type, Long id) {
        static ConversionResult internal() {
            return new ConversionResult("INTERNAL", null);
        }
    }
}
