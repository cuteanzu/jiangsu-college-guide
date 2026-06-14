package com.jiangsu.guide.service;

import com.jiangsu.guide.entity.School;
import com.jiangsu.guide.repository.CityRepository;
import com.jiangsu.guide.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV 数据导入服务
 *
 * 支持 city / school / school_life_info 三张表的 upsert 导入。
 * Upsert 策略:
 *   city           → 按 name (UNIQUE) 做 ON DUPLICATE KEY UPDATE
 *   school         → 按 code (UNIQUE) 做 ON DUPLICATE KEY UPDATE
 *   school_life_info → 按 school_id (UNIQUE) 做 ON DUPLICATE KEY UPDATE
 *
 * CSV 格式:
 *   cities.csv:          name,short_name,pinyin,description,tags,cost_guide,transit_guide,jobs_guide,audience,school_count,sort_order,map_x,map_y
 *   schools.csv:         code,name,city_name,type,level,lat,lng,founded,website,logo_url,address,brief,hot_score,map_x,map_y
 *   school_life_info.csv: school_code,dorm_score,dorm_desc,canteen_score,canteen_desc,study_score,study_desc,transport_score,transport_desc,surrounding_score,surrounding_desc,tips
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CsvImportService {

    private final JdbcTemplate jdbcTemplate;
    private final CityRepository cityRepository;
    private final SchoolRepository schoolRepository;

    // ================================================================
    // City upsert
    // ================================================================
    @Transactional
    public int importCities(InputStream inputStream) {
        List<String[]> rows = parseCsv(inputStream, 13);
        if (rows.isEmpty()) return 0;

        String sql = "INSERT INTO city (name, short_name, pinyin, description, tags, " +
                "cost_guide, transit_guide, jobs_guide, audience, school_count, sort_order, map_x, map_y) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "short_name = VALUES(short_name), pinyin = VALUES(pinyin), " +
                "description = VALUES(description), tags = VALUES(tags), " +
                "cost_guide = VALUES(cost_guide), transit_guide = VALUES(transit_guide), " +
                "jobs_guide = VALUES(jobs_guide), audience = VALUES(audience), " +
                "school_count = VALUES(school_count), sort_order = VALUES(sort_order), " +
                "map_x = VALUES(map_x), map_y = VALUES(map_y), updated_at = NOW()";

        jdbcTemplate.batchUpdate(sql, rows, 100, (PreparedStatement ps, String[] row) -> {
            ps.setString(1, row[0]);   // name
            ps.setString(2, row[1]);   // short_name
            ps.setString(3, row[2]);   // pinyin
            ps.setString(4, row[3]);   // description
            ps.setString(5, row[4]);   // tags
            ps.setString(6, row[5]);   // cost_guide
            ps.setString(7, row[6]);   // transit_guide
            ps.setString(8, row[7]);   // jobs_guide
            ps.setString(9, row[8]);   // audience
            ps.setInt(10, parseInt(row[9]));     // school_count
            ps.setInt(11, parseInt(row[10]));    // sort_order
            ps.setObject(12, parseDouble(row[11])); // map_x
            ps.setObject(13, parseDouble(row[12])); // map_y
        });

        log.info("✔ 城市导入完成: {} 行", rows.size());
        return rows.size();
    }

    // ================================================================
    // School upsert
    // ================================================================
    @Transactional
    public int importSchools(InputStream inputStream) {
        List<String[]> rows = parseCsv(inputStream, 15);
        if (rows.isEmpty()) return 0;

        String sql = "INSERT INTO school (code, name, city_id, type, level, lat, lng, founded, " +
                "website, logo_url, address, brief, hot_score, map_x, map_y) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "name = VALUES(name), city_id = VALUES(city_id), type = VALUES(type), " +
                "level = VALUES(level), lat = VALUES(lat), lng = VALUES(lng), " +
                "founded = VALUES(founded), website = VALUES(website), " +
                "logo_url = VALUES(logo_url), address = VALUES(address), " +
                "brief = VALUES(brief), hot_score = VALUES(hot_score), " +
                "map_x = VALUES(map_x), map_y = VALUES(map_y), updated_at = NOW()";

        jdbcTemplate.batchUpdate(sql, rows, 100, (PreparedStatement ps, String[] row) -> {
            ps.setString(1, row[0]);   // code
            ps.setString(2, row[1]);   // name
            ps.setLong(3, resolveCityId(row[2])); // city_name → city_id
            ps.setString(4, row[3]);   // type
            ps.setString(5, row[4]);   // level
            ps.setObject(6, parseDouble(row[5]));  // lat
            ps.setObject(7, parseDouble(row[6]));  // lng
            ps.setObject(8, parseIntOrNull(row[7])); // founded
            ps.setString(9, row[8]);   // website
            ps.setString(10, row[9]);  // logo_url
            ps.setString(11, row[10]); // address
            ps.setString(12, row[11]); // brief
            ps.setInt(13, parseInt(row[12]));  // hot_score
            ps.setObject(14, parseDouble(row[13])); // map_x
            ps.setObject(15, parseDouble(row[14])); // map_y
        });

        log.info("✔ 学校导入完成: {} 行", rows.size());
        return rows.size();
    }

    // ================================================================
    // SchoolLifeInfo upsert
    // ================================================================
    @Transactional
    public int importSchoolLifeInfo(InputStream inputStream) {
        List<String[]> rows = parseCsv(inputStream, 12);
        if (rows.isEmpty()) return 0;

        String sql = "INSERT INTO school_life_info (school_id, dorm_score, dorm_desc, " +
                "canteen_score, canteen_desc, study_score, study_desc, " +
                "transport_score, transport_desc, surrounding_score, surrounding_desc, tips) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "dorm_score = VALUES(dorm_score), dorm_desc = VALUES(dorm_desc), " +
                "canteen_score = VALUES(canteen_score), canteen_desc = VALUES(canteen_desc), " +
                "study_score = VALUES(study_score), study_desc = VALUES(study_desc), " +
                "transport_score = VALUES(transport_score), transport_desc = VALUES(transport_desc), " +
                "surrounding_score = VALUES(surrounding_score), surrounding_desc = VALUES(surrounding_desc), " +
                "tips = VALUES(tips), updated_at = NOW()";

        jdbcTemplate.batchUpdate(sql, rows, 100, (PreparedStatement ps, String[] row) -> {
            ps.setLong(1, resolveSchoolId(row[0])); // school_code → school_id
            ps.setObject(2, parseDouble(row[1]));   // dorm_score
            ps.setString(3, row[2]);                // dorm_desc
            ps.setObject(4, parseDouble(row[3]));   // canteen_score
            ps.setString(5, row[4]);                // canteen_desc
            ps.setObject(6, parseDouble(row[5]));   // study_score
            ps.setString(7, row[6]);                // study_desc
            ps.setObject(8, parseDouble(row[7]));   // transport_score
            ps.setString(9, row[8]);                // transport_desc
            ps.setObject(10, parseDouble(row[9]));  // surrounding_score
            ps.setString(11, row[10]);              // surrounding_desc
            ps.setString(12, row[11]);              // tips
        });

        log.info("✔ 生活信息导入完成: {} 行", rows.size());
        return rows.size();
    }

    // ================================================================
    // 辅助方法
    // ================================================================

    /**
     * 根据城市名称解析 city_id
     */
    private Long resolveCityId(String cityName) {
        return cityRepository.findByName(cityName.trim())
                .orElseThrow(() -> new IllegalArgumentException("城市不存在: " + cityName))
                .getId();
    }

    /**
     * 根据学校 code 解析 school_id
     */
    private Long resolveSchoolId(String schoolCode) {
        School school = schoolRepository.findByCode(schoolCode.trim()).orElse(null);
        if (school == null) {
            throw new IllegalArgumentException("学校不存在(code): " + schoolCode);
        }
        return school.getId();
    }

    /**
     * 解析 CSV 文件
     * 跳过空行和以 # 开头的注释行
     * 第一行为表头，自动跳过
     */
    private List<String[]> parseCsv(InputStream inputStream, int expectedColumns) {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            boolean isHeader = true;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                if (isHeader) { isHeader = false; continue; }

                String[] cols = parseLine(line);
                if (cols.length < expectedColumns) {
                    log.warn("第{}行列数不足(期望{}实际{}), 跳过: {}", lineNum, expectedColumns, cols.length, line);
                    continue;
                }
                rows.add(cols);
            }
        } catch (Exception e) {
            throw new RuntimeException("CSV 解析失败", e);
        }
        return rows;
    }

    /**
     * 解析一行 CSV（支持引号包裹的字段）
     */
    private String[] parseLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString().trim());
        return fields.toArray(new String[0]);
    }

    private int parseInt(String s) {
        return (s == null || s.isEmpty()) ? 0 : Integer.parseInt(s.trim());
    }

    private Integer parseIntOrNull(String s) {
        return (s == null || s.isEmpty()) ? null : Integer.parseInt(s.trim());
    }

    private Double parseDouble(String s) {
        return (s == null || s.isEmpty()) ? null : Double.parseDouble(s.trim());
    }
}
