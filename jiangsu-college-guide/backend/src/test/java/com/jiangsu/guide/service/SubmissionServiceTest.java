package com.jiangsu.guide.service;

import com.jiangsu.guide.entity.Experience;
import com.jiangsu.guide.entity.Submission;
import com.jiangsu.guide.repository.CommentRepository;
import com.jiangsu.guide.repository.ExperienceRepository;
import com.jiangsu.guide.repository.QaEntryRepository;
import com.jiangsu.guide.repository.SchoolLifeInfoRepository;
import com.jiangsu.guide.repository.SchoolRepository;
import com.jiangsu.guide.repository.SubmissionRepository;
import com.jiangsu.guide.service.impl.SubmissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private ExperienceRepository experienceRepository;
    @Mock
    private QaEntryRepository qaEntryRepository;
    @Mock
    private SchoolLifeInfoRepository schoolLifeInfoRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private SchoolRepository schoolRepository;

    private SubmissionService submissionService;

    @BeforeEach
    void setUp() {
        submissionService = new SubmissionServiceImpl(
                submissionRepository,
                experienceRepository,
                qaEntryRepository,
                schoolLifeInfoRepository,
                commentRepository,
                schoolRepository);
    }

    @Test
    void approveSubmission_shouldConvertExperienceToPublicContent() {
        Submission submission = Submission.builder()
                .id(10L)
                .userId(3L)
                .type("EXPERIENCE")
                .title("How dorm life works")
                .content("Dorm rooms are compact, but the library is easy to reach.")
                .isAnonymous(0)
                .status("PENDING")
                .build();

        when(submissionRepository.findById(10L)).thenReturn(Optional.of(submission));
        when(experienceRepository.findBySubmissionId(10L)).thenReturn(Optional.empty());
        when(experienceRepository.save(any(Experience.class))).thenAnswer(invocation -> {
            Experience experience = invocation.getArgument(0);
            experience.setId(101L);
            return experience;
        });

        submissionService.approveSubmission(10L, 7L);

        ArgumentCaptor<Experience> experienceCaptor = ArgumentCaptor.forClass(Experience.class);
        verify(experienceRepository).save(experienceCaptor.capture());
        Experience savedExperience = experienceCaptor.getValue();

        assertEquals("user-exp-10", savedExperience.getCode());
        assertEquals("dorm", savedExperience.getCategory());
        assertEquals("APPROVED", savedExperience.getStatus());
        assertEquals("USER_SUBMIT", savedExperience.getSourceType());
        assertEquals(10L, savedExperience.getSubmissionId());
        assertEquals("Dorm rooms are compact, but the library is easy to reach.", savedExperience.getBody());

        assertEquals("APPROVED", submission.getStatus());
        assertEquals(7L, submission.getReviewerId());
        assertEquals("EXPERIENCE", submission.getConvertedType());
        assertEquals(101L, submission.getConvertedId());
        assertNotNull(submission.getReviewedAt());
        verify(submissionRepository).save(submission);
    }
}
