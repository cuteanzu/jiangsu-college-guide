package com.jiangsu.guide.controller;

import com.jiangsu.guide.common.Result;
import com.jiangsu.guide.dto.ExperienceDTO;
import com.jiangsu.guide.service.ExperienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/experiences")
@RequiredArgsConstructor
public class ExperienceController {

    private final ExperienceService experienceService;

    @GetMapping
    public Result<List<ExperienceDTO>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Long schoolId) {
        return Result.ok(experienceService.getAllExperiences(category, city, schoolId));
    }

    @GetMapping("/{code}")
    public Result<ExperienceDTO> get(@PathVariable String code) {
        return Result.ok(experienceService.getByCode(code));
    }
}
