package com.jiangsu.guide.controller;

import com.jiangsu.guide.common.Result;
import com.jiangsu.guide.dto.SchoolDTO;
import com.jiangsu.guide.dto.SchoolDetailDTO;
import com.jiangsu.guide.dto.UniversityDTO;
import com.jiangsu.guide.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolService schoolService;

    @GetMapping
    public Result<List<SchoolDTO>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long city,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(schoolService.search(keyword, city, level, type, page, size));
    }

    @GetMapping("/hot")
    public Result<List<SchoolDTO>> hotSchools(@RequestParam(defaultValue = "6") int limit) {
        return Result.ok(schoolService.getHotSchools(limit));
    }

    @GetMapping("/{id}")
    public Result<SchoolDetailDTO> getSchool(@PathVariable Long id) {
        return Result.ok(schoolService.getSchoolDetail(id));
    }

    @GetMapping("/universities")
    public Result<List<UniversityDTO>> listUniversities() {
        return Result.ok(schoolService.getAllUniversities());
    }

    @GetMapping("/universities/{code}")
    public Result<UniversityDTO> getUniversity(@PathVariable String code) {
        return Result.ok(schoolService.getUniversityByCode(code));
    }
}
