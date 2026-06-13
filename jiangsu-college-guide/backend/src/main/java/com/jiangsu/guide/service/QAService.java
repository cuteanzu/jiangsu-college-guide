package com.jiangsu.guide.service;

import com.jiangsu.guide.dto.QADTO;

import java.util.List;

public interface QAService {
    List<QADTO> getAllQA(String category);
    QADTO getByCode(String code);
}
