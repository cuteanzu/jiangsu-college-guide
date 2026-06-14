package com.jiangsu.guide.service;

import com.jiangsu.guide.entity.School;
import com.jiangsu.guide.repository.CityRepository;
import com.jiangsu.guide.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiangsu.guide.entity.School;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    // SchoolLifeSurveySummary upsert (colleges.chat 生活调查数据)
    // ================================================================
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * CSV 列序（27 列）:
     *   0:name  1:url  2:答卷数  3:上床下桌  4:空调  5:独立卫浴
     *   6:早晚自习  7:晨跑  8:跑步打卡  9:寒暑假  10:外卖
     *   11:交通  12:洗衣机  13:校园网  14:断电断网  15:食堂
     *   16:热水  17:电瓶车  18:限电  19:通宵自习  20:电脑
     *   21:卡消费  22:银行卡  23:超市  24:快递  25:共享单车  26:门禁
     */
    @Transactional
    public int importLifeSurvey(InputStream inputStream) {
        List<String[]> rows = parseCsv(inputStream, 27);
        if (rows.isEmpty()) return 0;

        int skipped = 0;
        int skippedEmpty = 0;
        List<String[]> validRows = new ArrayList<>();

        for (String[] row : rows) {
            String schoolName = row[0].trim();
            if (schoolName.isEmpty()) {
                log.warn("学校名为空，跳过该行");
                skipped++;
                continue;
            }
            // 跳过响应数为 0 的空条目（避免覆盖有效数据）
            if (parseInt(row[2]) == 0) {
                skippedEmpty++;
                continue;
            }
            Long schoolId = resolveSchoolIdByName(schoolName);
            if (schoolId == null) {
                log.warn("学校未匹配: \"{}\"，跳过该行", schoolName);
                skipped++;
                continue;
            }
            // 将 schoolId 注入到 row 数组中（扩展为 28 列，最后一列为 schoolId）
            String[] ext = Arrays.copyOf(row, 28);
            ext[27] = String.valueOf(schoolId);
            validRows.add(ext);
        }

        if (validRows.isEmpty()) {
            log.warn("没有可导入的行（全部跳过），共 {} 行", rows.size());
            return 0;
        }

        // 按 response_count 升序排列，丰富数据最后写入（覆盖空数据）
        validRows.sort((a, b) -> Integer.compare(parseInt(a[2]), parseInt(b[2])));

        String sql = "INSERT INTO school_life_survey_summary (school_id, school_name, source_url, response_count, " +
                "dorm_summary, ac_summary, private_bath_summary, study_rule_summary, " +
                "running_summary, vacation_summary, delivery_summary, transport_summary, " +
                "laundry_summary, network_summary, power_network_summary, canteen_summary, " +
                "hot_water_summary, scooter_summary, power_limit_summary, overnight_study_summary, " +
                "computer_summary, payment_summary, bank_card_summary, supermarket_summary, " +
                "express_summary, shared_bike_summary, access_control_summary, raw_payload) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "school_name = VALUES(school_name), source_url = VALUES(source_url), " +
                "response_count = VALUES(response_count), " +
                "dorm_summary = VALUES(dorm_summary), ac_summary = VALUES(ac_summary), " +
                "private_bath_summary = VALUES(private_bath_summary), " +
                "study_rule_summary = VALUES(study_rule_summary), " +
                "running_summary = VALUES(running_summary), " +
                "vacation_summary = VALUES(vacation_summary), " +
                "delivery_summary = VALUES(delivery_summary), " +
                "transport_summary = VALUES(transport_summary), " +
                "laundry_summary = VALUES(laundry_summary), " +
                "network_summary = VALUES(network_summary), " +
                "power_network_summary = VALUES(power_network_summary), " +
                "canteen_summary = VALUES(canteen_summary), " +
                "hot_water_summary = VALUES(hot_water_summary), " +
                "scooter_summary = VALUES(scooter_summary), " +
                "power_limit_summary = VALUES(power_limit_summary), " +
                "overnight_study_summary = VALUES(overnight_study_summary), " +
                "computer_summary = VALUES(computer_summary), " +
                "payment_summary = VALUES(payment_summary), " +
                "bank_card_summary = VALUES(bank_card_summary), " +
                "supermarket_summary = VALUES(supermarket_summary), " +
                "express_summary = VALUES(express_summary), " +
                "shared_bike_summary = VALUES(shared_bike_summary), " +
                "access_control_summary = VALUES(access_control_summary), " +
                "raw_payload = VALUES(raw_payload), updated_at = NOW()";

        jdbcTemplate.batchUpdate(sql, validRows, 100, (PreparedStatement ps, String[] row) -> {
            ps.setLong(1, Long.parseLong(row[27]));    // school_id
            ps.setString(2, row[0].trim());             // school_name
            ps.setString(3, nvl(row[1]));               // source_url
            ps.setInt(4, parseInt(row[2]));             // response_count
            ps.setString(5, generateSummary(row[3]));   // dorm_summary (上床下桌)
            ps.setString(6, generateSummary(row[4]));   // ac_summary (空调)
            ps.setString(7, generateSummary(row[5]));   // private_bath_summary (独立卫浴)
            ps.setString(8, generateSummary(row[6]));   // study_rule_summary (早晚自习)
            ps.setString(9, mergeRunningSummary(row[7], row[8])); // running_summary (晨跑+跑步打卡)
            ps.setString(10, generateSummary(row[9]));  // vacation_summary (寒暑假)
            ps.setString(11, generateSummary(row[10])); // delivery_summary (外卖)
            ps.setString(12, generateSummary(row[11])); // transport_summary (交通)
            ps.setString(13, generateSummary(row[12])); // laundry_summary (洗衣机)
            ps.setString(14, generateSummary(row[13])); // network_summary (校园网)
            ps.setString(15, generateSummary(row[14])); // power_network_summary (断电断网)
            ps.setString(16, generateSummary(row[15])); // canteen_summary (食堂)
            ps.setString(17, generateSummary(row[16])); // hot_water_summary (热水)
            ps.setString(18, generateSummary(row[17])); // scooter_summary (电瓶车)
            ps.setString(19, generateSummary(row[18])); // power_limit_summary (限电)
            ps.setString(20, generateSummary(row[19])); // overnight_study_summary (通宵自习)
            ps.setString(21, generateSummary(row[20])); // computer_summary (电脑)
            ps.setString(22, generateSummary(row[21])); // payment_summary (卡消费)
            ps.setString(23, generateSummary(row[22])); // bank_card_summary (银行卡)
            ps.setString(24, generateSummary(row[23])); // supermarket_summary (超市)
            ps.setString(25, generateSummary(row[24])); // express_summary (快递)
            ps.setString(26, generateSummary(row[25])); // shared_bike_summary (共享单车)
            ps.setString(27, generateSummary(row[26])); // access_control_summary (门禁)
            ps.setString(28, buildRawPayload(row));     // raw_payload (JSON)
        });

        log.info("✔ 生活调查导入完成: 成功 {} 行, 跳过 {} 行 (未匹配: {}, 空数据: {})",
                validRows.size(), skipped + skippedEmpty, skipped, skippedEmpty);
        return validRows.size();
    }

    /**
     * 按学校名称匹配 school_id（多级策略）
     * 1. 精确匹配
     * 2. 模糊匹配：CSV 名称包含 DB 名称，或 DB 名称包含 CSV 名称
     * 3. 对分校区做前缀匹配（如 "南京大学仙林校区" → "南京大学"）
     */
    private Long resolveSchoolIdByName(String schoolName) {
        String name = schoolName.trim();
        if (name.isEmpty()) return null;

        // 第 1 级：精确匹配
        List<School> exact = schoolRepository.findByNameContaining(name);
        for (School s : exact) {
            if (s.getName().equals(name)) {
                return s.getId();
            }
        }

        // 第 2 级：双向包含匹配
        for (School s : exact) {
            if (name.contains(s.getName()) || s.getName().contains(name)) {
                log.info("模糊匹配: CSV \"{}\" → DB \"{}\"", name, s.getName());
                return s.getId();
            }
        }

        // 第 3 级：全表扫描 — 用 DB 名去匹配 CSV 名（处理分校区名称）
        List<School> allSchools = schoolRepository.findAll();
        // 按名称长度降序排列，优先匹配更具体的名称（如"南京理工大学泰州科技学院"优先于"南京理工大学"）
        allSchools.sort((a, b) -> Integer.compare(b.getName().length(), a.getName().length()));
        for (School s : allSchools) {
            if (name.contains(s.getName())) {
                log.info("子串匹配: CSV \"{}\" → DB \"{}\"", name, s.getName());
                return s.getId();
            }
        }

        return null;
    }

    /**
     * 生成单个调查维度的摘要
     * 输入: "回答1|回答2|回答3|回答1"
     * 输出: "回答1(2人); 回答2(1人); 回答3(1人)"
     */
    private String generateSummary(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        String[] parts = raw.split("\\|");
        Map<String, Integer> counter = new LinkedHashMap<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;
            counter.merge(trimmed, 1, Integer::sum);
        }
        if (counter.isEmpty()) return null;
        return counter.entrySet().stream()
                .limit(10)
                .map(e -> e.getKey() + (counter.size() > 1 || e.getValue() > 1 ? "(" + e.getValue() + "人)" : ""))
                .collect(Collectors.joining("; "));
    }

    /**
     * 合并晨跑和跑步打卡两个维度
     */
    private String mergeRunningSummary(String morningRun, String runCheckin) {
        StringBuilder sb = new StringBuilder();
        if (morningRun != null && !morningRun.trim().isEmpty()) {
            sb.append("【晨跑】").append(generateSummary(morningRun));
        }
        if (runCheckin != null && !runCheckin.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append("【跑步打卡】").append(generateSummary(runCheckin));
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * 将整行 CSV 数据构建为 JSON 字符串
     */
    private String buildRawPayload(String[] row) {
        Map<String, String> map = new LinkedHashMap<>();
        String[] keys = {
            "name", "url", "答卷数", "上床下桌", "空调", "独立卫浴",
            "早晚自习", "晨跑", "跑步打卡", "寒暑假", "外卖",
            "交通", "洗衣机", "校园网", "断电断网", "食堂",
            "热水", "电瓶车", "限电", "通宵自习", "电脑",
            "卡消费", "银行卡", "超市", "快递", "共享单车", "门禁"
        };
        for (int i = 0; i < keys.length && i < row.length; i++) {
            map.put(keys[i], row[i] != null ? row[i].trim() : "");
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.warn("JSON 序列化失败", e);
            return "{}";
        }
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

    private String nvl(String s) {
        return (s == null || s.isEmpty()) ? null : s.trim();
    }
}
