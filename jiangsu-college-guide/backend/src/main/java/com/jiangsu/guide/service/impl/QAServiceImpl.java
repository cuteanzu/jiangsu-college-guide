package com.jiangsu.guide.service.impl;

import com.jiangsu.guide.dto.QADTO;
import com.jiangsu.guide.entity.QaEntry;
import com.jiangsu.guide.entity.School;
import com.jiangsu.guide.exception.BusinessException;
import com.jiangsu.guide.repository.QaEntryRepository;
import com.jiangsu.guide.repository.SchoolRepository;
import com.jiangsu.guide.service.QAService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QAServiceImpl implements QAService {

    private final QaEntryRepository qaEntryRepository;
    private final SchoolRepository schoolRepository;

    @Override
    public List<QADTO> getAllQA(String category) {
        return qaEntryRepository.findByFilters(category)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public QADTO getByCode(String code) {
        QaEntry qa = qaEntryRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(404, "问答不存在"));
        return toDTO(qa);
    }

    private QADTO toDTO(QaEntry q) {
        String schoolCode = null;
        if (q.getSchoolId() != null) {
            schoolCode = schoolRepository.findById(q.getSchoolId())
                    .map(School::getCode)
                    .orElse(null);
        }

        return QADTO.builder()
                .id(q.getCode())
                .question(q.getQuestion())
                .answer(q.getAnswer())
                .schoolId(schoolCode)
                .schoolName(q.getSchoolName())
                .category(q.getCategory())
                .likes(q.getLikeCount())
                .build();
    }
}
