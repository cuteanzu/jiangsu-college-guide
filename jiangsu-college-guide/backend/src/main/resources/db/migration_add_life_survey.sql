-- ============================================================
-- Migration: 新增学校生活调查汇总表
-- 用于存储从 colleges.chat 导入的高校生活调查数据
-- 兼容 MySQL 8.0+ 和 H2 (MySQL 模式)
-- ============================================================

CREATE TABLE IF NOT EXISTS school_life_survey_summary (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    school_id       BIGINT        NOT NULL,
    school_name     VARCHAR(100)  NOT NULL,
    source_url      VARCHAR(500)  DEFAULT NULL,
    response_count  INT           DEFAULT 0,

    -- 23 个调查维度摘要
    dorm_summary              TEXT COMMENT '上床下桌',
    ac_summary                TEXT COMMENT '空调',
    private_bath_summary      TEXT COMMENT '独立卫浴',
    study_rule_summary        TEXT COMMENT '早晚自习',
    running_summary           TEXT COMMENT '晨跑 + 跑步打卡',
    vacation_summary          TEXT COMMENT '寒暑假',
    delivery_summary          TEXT COMMENT '外卖',
    transport_summary         TEXT COMMENT '交通',
    laundry_summary           TEXT COMMENT '洗衣机',
    network_summary           TEXT COMMENT '校园网',
    power_network_summary     TEXT COMMENT '断电断网',
    canteen_summary           TEXT COMMENT '食堂',
    hot_water_summary         TEXT COMMENT '热水',
    scooter_summary           TEXT COMMENT '电瓶车',
    power_limit_summary       TEXT COMMENT '限电',
    overnight_study_summary   TEXT COMMENT '通宵自习',
    computer_summary          TEXT COMMENT '电脑',
    payment_summary           TEXT COMMENT '卡消费',
    bank_card_summary         TEXT COMMENT '银行卡',
    supermarket_summary       TEXT COMMENT '超市',
    express_summary           TEXT COMMENT '快递',
    shared_bike_summary       TEXT COMMENT '共享单车',
    access_control_summary    TEXT COMMENT '门禁',

    raw_payload     TEXT COMMENT '原始 CSV 行数据 (JSON 格式)',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_survey_school (school_id),
    INDEX idx_survey_school_name (school_name),
    CONSTRAINT fk_survey_school FOREIGN KEY (school_id) REFERENCES school(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
