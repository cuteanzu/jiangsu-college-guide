package com.jiangsu.guide.controller;

import com.jiangsu.guide.common.Result;
import com.jiangsu.guide.dto.QADTO;
import com.jiangsu.guide.service.QAService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qa")
@RequiredArgsConstructor
public class QAController {

    private final QAService qaService;

    @GetMapping
    public Result<List<QADTO>> list(
            @RequestParam(required = false) String category) {
        return Result.ok(qaService.getAllQA(category));
    }

    @GetMapping("/{code}")
    public Result<QADTO> get(@PathVariable String code) {
        return Result.ok(qaService.getByCode(code));
    }
}
