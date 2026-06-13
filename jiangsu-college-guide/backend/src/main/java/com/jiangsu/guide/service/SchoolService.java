package com.jiangsu.guide.service;

import com.jiangsu.guide.dto.SchoolDTO;
import com.jiangsu.guide.dto.SchoolDetailDTO;
import com.jiangsu.guide.dto.UniversityDTO;

import java.util.List;

public interface SchoolService {
    SchoolDetailDTO getSchoolDetail(Long id);
    List<SchoolDTO> getHotSchools(int limit);
    List<SchoolDTO> search(String keyword, Long cityId, String level, String type, int page, int size);
    List<SchoolDTO> getSchoolsByCity(Long cityId);
    List<UniversityDTO> getAllUniversities();
    UniversityDTO getUniversityByCode(String code);
    List<UniversityDTO> getUniversitiesByCity(Long cityId);
}
