-- ============================================================
-- 江苏高校生活指北可视化平台 - 数据库初始化脚本
-- ============================================================

-- ============================================================
-- 1. 用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS `app_user` (
    `id`            BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `username`      VARCHAR(50)     NOT NULL UNIQUE        COMMENT '用户名',
    `password`      VARCHAR(255)    NOT NULL               COMMENT '密码(BCrypt)',
    `nickname`      VARCHAR(50)     DEFAULT NULL           COMMENT '昵称',
    `avatar`        VARCHAR(500)    DEFAULT NULL           COMMENT '头像URL',
    `email`         VARCHAR(100)    DEFAULT NULL           COMMENT '邮箱',
    `role`          VARCHAR(20)     NOT NULL DEFAULT 'USER' COMMENT '角色: USER / ADMIN',
    `status`        TINYINT         NOT NULL DEFAULT 1     COMMENT '状态: 1=正常, 0=禁用',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_role (`role`),
    INDEX idx_user_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 2. 城市表
-- ============================================================
CREATE TABLE IF NOT EXISTS `city` (
    `id`            BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `name`          VARCHAR(50)     NOT NULL UNIQUE        COMMENT '城市名',
    `short_name`    VARCHAR(50)     DEFAULT NULL           COMMENT '简称',
    `pinyin`        VARCHAR(100)    DEFAULT NULL           COMMENT '拼音',
    `province`      VARCHAR(50)     NOT NULL DEFAULT '江苏省' COMMENT '省份',
    `description`   TEXT            DEFAULT NULL           COMMENT '城市简介',
    `tags`          VARCHAR(300)    DEFAULT NULL           COMMENT '标签(逗号分隔)',
    `cost_guide`    VARCHAR(50)     DEFAULT NULL           COMMENT '生活成本',
    `transit_guide` VARCHAR(50)     DEFAULT NULL           COMMENT '交通便利度',
    `jobs_guide`    VARCHAR(50)     DEFAULT NULL           COMMENT '就业机会',
    `audience`      TEXT            DEFAULT NULL           COMMENT '适合人群',
    `school_count`  INT             NOT NULL DEFAULT 0     COMMENT '高校数量',
    `sort_order`    INT             NOT NULL DEFAULT 0     COMMENT '排序',
    `map_x`         DOUBLE          DEFAULT NULL           COMMENT '地图坐标X(%)',
    `map_y`         DOUBLE          DEFAULT NULL           COMMENT '地图坐标Y(%)',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_city_sort (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='城市表';

-- ============================================================
-- 3. 学校表
-- ============================================================
CREATE TABLE IF NOT EXISTS `school` (
    `id`            BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `code`          VARCHAR(30)     NOT NULL UNIQUE        COMMENT '英文标识码(nju/seu等)',
    `name`          VARCHAR(100)    NOT NULL               COMMENT '学校名称',
    `city_id`       BIGINT          NOT NULL               COMMENT '所属城市ID',
    `type`          VARCHAR(20)     DEFAULT NULL           COMMENT '类型: 综合/理工/师范/医药/农林/艺术/体育',
    `level`         VARCHAR(20)     DEFAULT NULL           COMMENT '层次: 985/211/dual/provincial',
    `lat`           DOUBLE          DEFAULT NULL           COMMENT '纬度',
    `lng`           DOUBLE          DEFAULT NULL           COMMENT '经度',
    `founded`       INT             DEFAULT NULL           COMMENT '建校年份',
    `logo_url`      VARCHAR(500)    DEFAULT NULL           COMMENT '校徽URL',
    `cover_url`     VARCHAR(500)    DEFAULT NULL           COMMENT '封面图URL',
    `website`       VARCHAR(200)    DEFAULT NULL           COMMENT '官网',
    `address`       VARCHAR(300)    DEFAULT NULL           COMMENT '地址',
    `brief`         TEXT            DEFAULT NULL           COMMENT '简介',
    `hot_score`     INT             NOT NULL DEFAULT 0     COMMENT '热度分数',
    `favorite_count` INT            NOT NULL DEFAULT 0     COMMENT '收藏数',
    `map_x`         DOUBLE          DEFAULT NULL           COMMENT '地图坐标X(%)',
    `map_y`         DOUBLE          DEFAULT NULL           COMMENT '地图坐标Y(%)',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_school_city (`city_id`),
    INDEX idx_school_level (`level`),
    INDEX idx_school_hot (`hot_score`),
    INDEX idx_school_name (`name`),
    INDEX idx_school_code (`code`),
    CONSTRAINT fk_school_city FOREIGN KEY (`city_id`) REFERENCES `city`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学校表';

-- ============================================================
-- 4. 学校生活信息表
-- ============================================================
CREATE TABLE IF NOT EXISTS `school_life_info` (
    `id`                BIGINT      AUTO_INCREMENT PRIMARY KEY,
    `school_id`         BIGINT      NOT NULL               COMMENT '学校ID',
    `dorm_score`        DOUBLE      DEFAULT NULL           COMMENT '宿舍评分(1-10)',
    `dorm_desc`         TEXT        DEFAULT NULL           COMMENT '宿舍描述',
    `canteen_score`     DOUBLE      DEFAULT NULL           COMMENT '食堂评分(1-10)',
    `canteen_desc`      TEXT        DEFAULT NULL           COMMENT '食堂描述',
    `study_score`       DOUBLE      DEFAULT NULL           COMMENT '学习氛围评分(1-10)',
    `study_desc`        TEXT        DEFAULT NULL           COMMENT '学习氛围描述',
    `transport_score`   DOUBLE      DEFAULT NULL           COMMENT '交通评分(1-10)',
    `transport_desc`    TEXT        DEFAULT NULL           COMMENT '交通描述',
    `surrounding_score` DOUBLE      DEFAULT NULL           COMMENT '周边评分(1-10)',
    `surrounding_desc`  TEXT        DEFAULT NULL           COMMENT '周边描述',
    `tips`              TEXT        DEFAULT NULL           COMMENT '新生建议',
    `source_type`       VARCHAR(20) DEFAULT 'OFFICIAL'     COMMENT '来源: OFFICIAL/USER_SUBMIT',
    `audit_status`      VARCHAR(20) DEFAULT 'APPROVED'     COMMENT '审核状态: PENDING/APPROVED/REJECTED',
    `created_at`        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_school_life (`school_id`),
    CONSTRAINT fk_life_school FOREIGN KEY (`school_id`) REFERENCES `school`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学校生活信息表';

-- ============================================================
-- 5. 评论留言表
-- ============================================================
CREATE TABLE IF NOT EXISTS `comment` (
    `id`            BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `school_id`     BIGINT          NOT NULL               COMMENT '学校ID',
    `user_id`       BIGINT          NOT NULL               COMMENT '用户ID',
    `content`       TEXT            NOT NULL               COMMENT '内容',
    `category`      VARCHAR(30)     DEFAULT 'GENERAL'      COMMENT '分类: GENERAL/DORM/CANTEEN/STUDY/TRANSPORT/SURROUNDING/CORRECTION',
    `is_anonymous`  TINYINT         NOT NULL DEFAULT 0     COMMENT '是否匿名: 0=否, 1=是',
    `like_count`    INT             NOT NULL DEFAULT 0     COMMENT '点赞数',
    `status`        VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/APPROVED/REJECTED',
    `reject_reason` VARCHAR(500)    DEFAULT NULL           COMMENT '驳回原因',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_comment_school (`school_id`),
    INDEX idx_comment_user (`user_id`),
    INDEX idx_comment_status (`status`),
    CONSTRAINT fk_comment_school FOREIGN KEY (`school_id`) REFERENCES `school`(`id`),
    CONSTRAINT fk_comment_user FOREIGN KEY (`user_id`) REFERENCES `app_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论留言表';

-- ============================================================
-- 6. 投稿表
-- ============================================================
CREATE TABLE IF NOT EXISTS `submission` (
    `id`            BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `user_id`       BIGINT          NOT NULL               COMMENT '投稿用户ID',
    `school_id`     BIGINT          DEFAULT NULL           COMMENT '关联学校ID(可为空)',
    `school_name`   VARCHAR(100)    DEFAULT NULL           COMMENT '学校名(未关联时)',
    `type`          VARCHAR(30)     NOT NULL               COMMENT '类型: NEW_INFO/CORRECTION/EXPERIENCE/DORM/CANTEEN/TRANSPORT/STUDY/SURROUNDING',
    `title`         VARCHAR(200)    DEFAULT NULL           COMMENT '标题',
    `content`       TEXT            NOT NULL               COMMENT '内容',
    `is_anonymous`  TINYINT         NOT NULL DEFAULT 0     COMMENT '是否匿名',
    `contact`       VARCHAR(100)    DEFAULT NULL           COMMENT '联系方式',
    `status`        VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/APPROVED/REJECTED',
    `reviewer_id`   BIGINT          DEFAULT NULL           COMMENT '审核人ID',
    `reject_reason` VARCHAR(500)    DEFAULT NULL           COMMENT '驳回原因',
    `reviewed_at`   DATETIME        DEFAULT NULL           COMMENT '审核时间',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_sub_user (`user_id`),
    INDEX idx_sub_school (`school_id`),
    INDEX idx_sub_status (`status`),
    CONSTRAINT fk_sub_user FOREIGN KEY (`user_id`) REFERENCES `app_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投稿表';

-- ============================================================
-- 7. 收藏表
-- ============================================================
CREATE TABLE IF NOT EXISTS `favorite` (
    `id`            BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `user_id`       BIGINT          NOT NULL               COMMENT '用户ID',
    `school_id`     BIGINT          NOT NULL               COMMENT '学校ID',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_school (`user_id`, `school_id`),
    INDEX idx_fav_user (`user_id`),
    INDEX idx_fav_school (`school_id`),
    CONSTRAINT fk_fav_user FOREIGN KEY (`user_id`) REFERENCES `app_user`(`id`),
    CONSTRAINT fk_fav_school FOREIGN KEY (`school_id`) REFERENCES `school`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- ============================================================
-- 8. 校园经验文章表 (新增)
-- ============================================================
CREATE TABLE IF NOT EXISTS `experience` (
    `id`              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    `code`            VARCHAR(50)  NOT NULL UNIQUE        COMMENT '标识码(exp-1等)',
    `category`        VARCHAR(30)  NOT NULL               COMMENT '分类: dorm/cafeteria/study/freshman/city-life/exam/career',
    `school_id`       BIGINT       DEFAULT NULL           COMMENT '关联学校ID',
    `school_name`     VARCHAR(100) DEFAULT NULL           COMMENT '学校名称',
    `city`            VARCHAR(50)  DEFAULT NULL           COMMENT '城市名',
    `title`           VARCHAR(200) NOT NULL               COMMENT '标题',
    `excerpt`         VARCHAR(500) DEFAULT NULL           COMMENT '摘要',
    `body`            TEXT         NOT NULL               COMMENT '正文',
    `like_count`      INT          NOT NULL DEFAULT 0     COMMENT '点赞数',
    `comment_count`   INT          NOT NULL DEFAULT 0     COMMENT '评论数',
    `tags`            VARCHAR(500) DEFAULT NULL           COMMENT '标签(逗号分隔)',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_exp_category (`category`),
    INDEX idx_exp_school (`school_id`),
    INDEX idx_exp_city (`city`),
    CONSTRAINT fk_exp_school FOREIGN KEY (`school_id`) REFERENCES `school`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校园经验文章表';

-- ============================================================
-- 9. 问答表 (新增)
-- ============================================================
CREATE TABLE IF NOT EXISTS `qa_entry` (
    `id`              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    `code`            VARCHAR(50)  NOT NULL UNIQUE        COMMENT '标识码(qa-1等)',
    `question`        TEXT         NOT NULL               COMMENT '问题',
    `answer`          TEXT         NOT NULL               COMMENT '回答',
    `school_id`       BIGINT       DEFAULT NULL           COMMENT '关联学校ID',
    `school_name`     VARCHAR(100) DEFAULT NULL           COMMENT '学校名称',
    `category`        VARCHAR(30)  NOT NULL               COMMENT '分类',
    `like_count`      INT          NOT NULL DEFAULT 0     COMMENT '点赞数',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_qa_category (`category`),
    INDEX idx_qa_school (`school_id`),
    CONSTRAINT fk_qa_school FOREIGN KEY (`school_id`) REFERENCES `school`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问答表';

-- ============================================================
-- 初始数据
-- ============================================================

-- ── 13个城市 (含城市画像数据) ──
INSERT INTO `city` (`name`, `short_name`, `pinyin`, `description`, `tags`, `cost_guide`, `transit_guide`, `jobs_guide`, `audience`, `school_count`, `sort_order`) VALUES
('南京',   '南京', 'nanjing',   '江苏省省会，六朝古都，高校云集。拥有南京大学、东南大学等众多知名学府。', '省会资源,科研氛围,实习密集', '中高', '很便利', '很丰富', '适合目标明确、想接触更多科研平台和城市资源的同学', 26, 1),
('苏州',   '苏州', 'suzhou',    '江南水乡，经济强市。苏州大学、西交利物浦等高校坐落于此。', '园林校园,产业强市,城市品质', '较高', '便利', '很丰富', '适合喜欢精致城市、外企资源和产业机会的同学', 6, 2),
('无锡',   '无锡', 'wuxi',      '太湖明珠，物联网之都。江南大学等高校所在地。', '太湖生活,宜居节奏,产业实习', '中等', '便利', '丰富', '适合想兼顾舒适城市、产业机会和校园生活品质的同学', 3, 3),
('常州',   '常州', 'changzhou', '工业名城，常州大学等高校所在地。', '制造业强,城市紧凑,生活轻松', '中等', '便利', '稳定', '适合偏应用型专业、想要稳定发展和低通勤压力的同学', 3, 4),
('徐州',   '徐州', 'xuzhou',    '淮海经济区中心城市，中国矿业大学所在地。', '学风扎实,生活友好,考研氛围', '友好', '便利', '稳步增长', '适合重视性价比、踏实学风和北方生活气质的同学', 4, 5),
('南通',   '南通', 'nantong',   '江海门户，南通大学等高校所在地。', '江海城市,生活清爽,成长空间', '中等', '较便利', '发展中', '适合喜欢安静校园、清爽城市和成长型机会的同学', 2, 6),
('扬州',   '扬州', 'yangzhou',  '历史文化名城，扬州大学所在地。', '淮扬生活,历史名城,节奏舒缓', '友好', '便利', '稳定', '适合重视生活幸福感、师范农学和慢节奏城市的同学', 1, 7),
('镇江',   '镇江', 'zhenjiang', '江苏大学、江苏科技大学等高校所在地。', '山水校园,南京近邻,工科底色', '友好', '便利', '稳定', '适合喜欢紧凑城市、工科院校和宁镇通勤资源的同学', 2, 8),
('盐城',   '盐城', 'yancheng',  '东方湿地之都，盐城工学院等高校所在地。', '沿海湿地,生活成本低,基础扎实', '友好', '较便利', '发展中', '适合想要低生活压力、安静校园和地方产业机会的同学', 2, 9),
('泰州',   '泰州', 'taizhou',   '医药名城，泰州学院等高校所在地。', '医药产业,城市安静,生活宜居', '友好', '较便利', '稳定', '适合关注医药健康产业、喜欢安静学习环境的同学', 2, 10),
('淮安',   '淮安', 'huaian',    '运河之都，淮阴工学院等高校所在地。', '运河城市,师范工科,物价友好', '友好', '便利', '稳定', '适合重视生活成本、师范工科和城市烟火气的同学', 2, 11),
('连云港', '连云港', 'lianyungang', '一带一路交汇点，江苏海洋大学所在地。', '山海城市,港口资源,空气清爽', '友好', '较便利', '发展中', '适合喜欢山海风光、海洋类专业和慢节奏校园的同学', 1, 12),
('宿迁',   '宿迁', 'suqian',    '项王故里，宿迁学院等高校所在地。', '新兴城市,成本低,校园专注', '低', '便利', '成长中', '适合预算敏感、想要专注读书和稳步成长的同学', 1, 13);

-- ── 55所本科高校 ──
-- 南京 (city_id=1) - 26所
INSERT INTO `school` (`code`, `name`, `city_id`, `level`, `lat`, `lng`, `founded`, `website`, `hot_score`) VALUES
('nju',     '南京大学',               1, '985',        32.1190, 118.9532, 1902, 'https://www.nju.edu.cn', 100),
('seu',     '东南大学',               1, '985',        31.8860, 118.8158, 1902, NULL, 95),
('nuaa',    '南京航空航天大学',       1, '211',        31.9387, 118.7929, 1952, NULL, 90),
('njust',   '南京理工大学',           1, '211',        32.0353, 118.8559, 1953, NULL, 88),
('hhu',     '河海大学',               1, '211',        31.9103, 118.7591, 1915, NULL, 85),
('njau',    '南京农业大学',           1, '211',        32.0376, 118.8416, 1902, NULL, 82),
('cpu',     '中国药科大学',           1, '211',        31.8992, 118.9112, 1936, NULL, 80),
('njnu',    '南京师范大学',           1, '211',        32.1044, 118.9111, 1902, NULL, 86),
('nuist',   '南京信息工程大学',       1, 'dual',       32.2065, 118.7173, 1960, NULL, 78),
('njupt',   '南京邮电大学',           1, 'dual',       32.0800, 118.9458, 1942, NULL, 76),
('njfu',    '南京林业大学',           1, 'dual',       32.0793, 118.8135, 1902, NULL, 72),
('njmu',    '南京医科大学',           1, 'dual',       31.9460, 118.7621, 1934, NULL, 74),
('njucm',   '南京中医药大学',         1, 'dual',       32.0972, 118.9536, 1954, NULL, 70),
('njtech2', '南京工业大学',           1, 'provincial', 32.0789, 118.6489, NULL, NULL, 65),
('njue',    '南京财经大学',           1, 'provincial', 32.0930, 118.9118, NULL, NULL, 62),
('njaudit', '南京审计大学',           1, 'provincial', 32.0420, 118.5938, NULL, NULL, 60),
('njit',    '南京工程学院',           1, 'provincial', 31.9343, 118.8916, NULL, NULL, 55),
('njxzc',   '南京晓庄学院',           1, 'provincial', 31.9300, 118.9033, NULL, NULL, 50),
('njty',    '南京体育学院',           1, 'provincial', 32.0396, 118.8175, NULL, NULL, 48),
('nua',     '南京艺术学院',           1, 'provincial', 32.0540, 118.7480, NULL, NULL, 52),
('njpu',    '南京警察学院',           1, 'provincial', 32.0890, 118.7973, NULL, NULL, 46),
('jssnu',   '江苏第二师范学院',       1, 'provincial', 32.0843, 118.7939, NULL, NULL, 44),
('njtech',  '南京工业职业技术大学',   1, 'provincial', 32.1318, 118.9602, NULL, NULL, 42),
('nju_jl',  '南京大学金陵学院',       1, 'provincial', 32.1450, 118.7203, NULL, NULL, 40),
('seu_cx',  '东南大学成贤学院',       1, 'provincial', 32.0750, 118.7290, NULL, NULL, 38),
('nuaa_jc', '南航金城学院',           1, 'provincial', 31.8509, 118.7861, NULL, NULL, 36);

-- 苏州 (city_id=2) - 6所
INSERT INTO `school` (`code`, `name`, `city_id`, `level`, `lat`, `lng`, `founded`, `website`, `hot_score`) VALUES
('suda',   '苏州大学',           2, '211',        31.3066, 120.6387, 1900, NULL, 88),
('usts',   '苏州科技大学',       2, 'provincial', 31.2730, 120.5672, NULL, NULL, 55),
('cit',    '常熟理工学院',       2, 'provincial', 31.6580, 120.7437, NULL, NULL, 48),
('sit',    '苏州城市学院',       2, 'provincial', 31.2957, 120.5744, NULL, NULL, 44),
('kdsu',   '昆山杜克大学',       2, 'provincial', 31.3940, 120.9083, NULL, NULL, 58),
('xjtlu',  '西交利物浦大学',     2, 'provincial', 31.2730, 120.7400, NULL, NULL, 62);

-- 无锡 (city_id=3) - 3所
INSERT INTO `school` (`code`, `name`, `city_id`, `level`, `lat`, `lng`, `founded`, `website`, `hot_score`) VALUES
('jiangnan', '江南大学',     3, '211',        31.4909, 120.2730, 1958, NULL, 82),
('wxit',     '无锡学院',     3, 'provincial', 31.5023, 120.3908, NULL, NULL, 45),
('thnu',     '太湖学院',     3, 'provincial', 31.5320, 120.2625, NULL, NULL, 42);

-- 常州 (city_id=4) - 3所
INSERT INTO `school` (`code`, `name`, `city_id`, `level`, `lat`, `lng`, `founded`, `website`, `hot_score`) VALUES
('cczu', '常州大学',       4, 'provincial', 31.8113, 119.9587, NULL, NULL, 52),
('jsut', '江苏理工学院',   4, 'provincial', 31.7823, 119.9728, NULL, NULL, 48),
('czu',  '常州工学院',     4, 'provincial', 31.8222, 119.9940, NULL, NULL, 45);

-- 徐州 (city_id=5) - 4所
INSERT INTO `school` (`code`, `name`, `city_id`, `level`, `lat`, `lng`, `founded`, `website`, `hot_score`) VALUES
('cumt', '中国矿业大学',   5, '211',        34.2188, 117.1945, 1909, NULL, 80),
('jsnu', '江苏师范大学',   5, 'provincial', 34.2065, 117.1755, NULL, NULL, 55),
('xzmc', '徐州医科大学',   5, 'provincial', 34.2617, 117.1882, NULL, NULL, 52),
('xzit', '徐州工程学院',   5, 'provincial', 34.2430, 117.2803, NULL, NULL, 45);

-- 南通 (city_id=6) - 2所
INSERT INTO `school` (`code`, `name`, `city_id`, `level`, `lat`, `lng`, `founded`, `website`, `hot_score`) VALUES
('ntu',  '南通大学',       6, 'provincial', 31.9839, 120.9103, NULL, NULL, 55),
('ntit', '南通理工学院',   6, 'provincial', 32.0011, 120.8807, NULL, NULL, 42);

-- 扬州 (city_id=7) - 1所
INSERT INTO `school` (`code`, `name`, `city_id`, `level`, `lat`, `lng`, `founded`, `website`, `hot_score`) VALUES
('yzu', '扬州大学',        7, 'provincial', 32.3900, 119.4258, NULL, NULL, 62);

-- 镇江 (city_id=8) - 2所
INSERT INTO `school` (`code`, `name`, `city_id`, `level`, `lat`, `lng`, `founded`, `website`, `hot_score`) VALUES
('ujs',  '江苏大学',       8, 'provincial', 32.2010, 119.5119, NULL, NULL, 65),
('just', '江苏科技大学',   8, 'provincial', 32.1881, 119.4643, NULL, NULL, 55);

-- 盐城 (city_id=9) - 2所
INSERT INTO `school` (`code`, `name`, `city_id`, `level`, `lat`, `lng`, `founded`, `website`, `hot_score`) VALUES
('ycit', '盐城工学院',     9, 'provincial', 33.3136, 120.1657, NULL, NULL, 48),
('yctu', '盐城师范学院',   9, 'provincial', 33.3865, 120.1619, NULL, NULL, 45);

-- 泰州 (city_id=10) - 2所
INSERT INTO `school` (`code`, `name`, `city_id`, `level`, `lat`, `lng`, `founded`, `website`, `hot_score`) VALUES
('tzuh',   '泰州学院',                   10, 'provincial', 32.4732, 119.9238, NULL, NULL, 44),
('nusttz', '南京理工大学泰州科技学院',   10, 'provincial', 32.4795, 119.9353, NULL, NULL, 40);

-- 淮安 (city_id=11) - 2所
INSERT INTO `school` (`code`, `name`, `city_id`, `level`, `lat`, `lng`, `founded`, `website`, `hot_score`) VALUES
('hyit', '淮阴工学院',     11, 'provincial', 33.5851, 119.0241, NULL, NULL, 45),
('hytc', '淮阴师范学院',   11, 'provincial', 33.6075, 119.0434, NULL, NULL, 44);

-- 连云港 (city_id=12) - 1所
INSERT INTO `school` (`code`, `name`, `city_id`, `level`, `lat`, `lng`, `founded`, `website`, `hot_score`) VALUES
('jou', '江苏海洋大学',    12, 'provincial', 34.6022, 119.2365, NULL, NULL, 50);

-- 宿迁 (city_id=13) - 1所
INSERT INTO `school` (`code`, `name`, `city_id`, `level`, `lat`, `lng`, `founded`, `website`, `hot_score`) VALUES
('sqc', '宿迁学院',        13, 'provincial', 33.9469, 118.3205, NULL, NULL, 42);

-- ── 10篇校园经验文章 ──
INSERT INTO `experience` (`code`, `category`, `school_id`, `school_name`, `city`, `title`, `excerpt`, `body`, `like_count`, `comment_count`, `tags`) VALUES
('exp-1', 'dorm', (SELECT id FROM school WHERE code='nju'), '南京大学', '南京',
 '住得好才能学得好——南大仙林宿舍全攻略',
 '仙林校区的宿舍条件在江苏高校中算第一梯队，但选房也有一些门道。独立卫浴、空调、热水器一应俱全……',
 '南京大学仙林校区的本科生宿舍以四人间为主，上床下桌配置。每栋宿舍楼都配备了独立卫浴、空调和热水器，24 小时热水供应。

选房建议：东区宿舍楼较新但离教学楼略远，西区宿舍离食堂和超市更近。研究生宿舍集中在鼓楼校区，老建筑改造的房间别有韵味，但设施相对老旧。

宿舍楼每层都有自习室和洗衣房，一楼有公共厨房。宿管阿姨普遍温和，查寝频率不算高。唯一的槽点是仙林冬天风大，北向宿舍会偏冷。

整体评分：⭐⭐⭐⭐ 推荐新生优先选择东区新建宿舍楼。',
 56, 24, '宿舍,仙林校区,新生必看'),

('exp-2', 'cafeteria', (SELECT id FROM school WHERE code='seu'), '东南大学', '南京',
 '九龙湖的隐藏美食——东大食堂深度测评',
 '东大九龙湖校区有四个食堂，每个都有自己的招牌。梅园的铁板饭、橘园的麻辣香锅，还有桃园的早餐煎饼……',
 '东南大学九龙湖校区共有梅、兰、竹、菊四个食堂，外加桃园教工食堂。每个食堂都有自己的王牌窗口。

梅园食堂：铁板饭和麻辣烫是两大招牌，人均 15 元吃饱。二楼的小炒窗口现点现做，锅气十足。
橘园食堂：麻辣香锅窗口排队最长，自选食材现炒，辣度可调。隔壁的奶茶店是自习后的救星。
桃园食堂：早餐最佳选择，煎饼果子和豆腐脑广受好评。早八人的精神支柱。

省錢技巧：食堂支持校园卡和支付宝，周末部分窗口有特价菜。整体吃一个月伙食费大约 800-1200 元。',
 42, 18, '食堂,九龙湖,美食'),

('exp-3', 'freshman', (SELECT id FROM school WHERE code='suda'), '苏州大学', '苏州',
 '入学前最该知道的——苏大新生生存指南',
 '苏大本部在古城区，环境美得像园林，但生活配套和想象中不太一样。这几点经验希望你来之前就知道……',
 '苏州大学有四个校区：本部（天赐庄）、东区、北区、独墅湖。新生最容易被校区搞晕。

本部在老城区，苏式园林建筑美到窒息，但宿舍条件参差不齐。独墅湖校区现代化程度最高，设施也最新。选宿舍时尽量选独墅湖校区。

苏州的节奏比南京慢，但生活成本不低。学校周边租房贵，建议新生第一年住校适应。苏州话听不懂很正常——老师上课都用普通话。

出行方面：苏州地铁覆盖主要校区，本部到独墅湖大约 40 分钟。共享单车在校园内随处可见。苏州冬天湿冷，北方同学做好心理准备。

推荐新生关注"苏大微生活"公众号，选课攻略和活动信息都有。',
 78, 32, '新生,苏州,避坑'),

('exp-4', 'study', (SELECT id FROM school WHERE code='njnu'), '南京师范大学', '南京',
 '在南师读中文系是一种什么体验',
 '南师的中国语言文学是国家重点学科，但真实的课堂、作业量和学术氛围，和你想象的可能不太一样……',
 '南京师范大学文学院是南师的王牌院系之一。中国语言文学专业在第四轮学科评估中获评 A 类，师资力量雄厚。

课堂体验：古代文学课是重头戏，教授们引经据典信手拈来。现当代文学方向更活跃，课堂讨论氛围好。每学期至少两篇课程论文，大三开始有学年论文。

自习去处：随园校区的古籍阅览室环境极佳，藏书量在江苏高校中数一数二。仙林校区的敬文图书馆座位充足，考试周需要早起占座。

学术资源：文学院定期请知名作家和学者来讲座，莫言、余华都来过。研究生推免名额较多，保研率在师范类院校中偏高。

如果你真心喜欢文学，南师不会让你失望。但如果只是为了文凭，课程压力可能会让你吃力。',
 35, 14, '专业学习,中文系,文科'),

('exp-5', 'city-life', (SELECT id FROM school WHERE code='jiangnan'), '江南大学', '无锡',
 '在无锡读书的四年——一座被低估的宝藏城市',
 '很多人对无锡的印象只有鼋头渚和甜排骨，但在这座城市生活四年后，我发现它比想象中舒服太多……',
 '江南大学位于无锡滨湖区，太湖之滨。校园本身就够美——被称为"江南第一学府"并非浪得虚名。

无锡的生活节奏介于南京的忙碌和苏州的安逸之间。地铁 4 条线覆盖市区，从江大到市中心大约半小时。太湖新城商圈这几年发展很快，吃喝玩乐应有尽有。

推荐去处：蠡湖公园骑行、南长街夜市、惠山古镇喝茶。春天鼋头渚樱花，秋天拈花湾。无锡景点学生票半价。

生活成本：月均 1500-2000 元够用。租房预算 1000-1500/月能租到不错的单间。无锡的工资水平在江苏仅次于苏州南京，实习机会不少。

如果你追求不太卷、有品质感的大学生活，无锡是很好的选择。',
 41, 19, '城市生活,无锡,宜居'),

('exp-6', 'exam', (SELECT id FROM school WHERE code='cumt'), '中国矿业大学', '徐州',
 '矿大考研上岸指南——从双一流到 985 的逆袭路径',
 '矿大的考研氛围在江苏高校中数一数二。图书馆自习室常年满座，大四考研率超过 60%。这份经验写给想冲 985 的你……',
 '中国矿业大学（徐州）虽然地理位置不占优，但学风极其扎实。学校保研率约 15%，考研成功率在省内双一流高校中名列前茅。

热门考研去向：南京大学、东南大学、同济大学、浙江大学。工科生尤其受 985 院校青睐。矿大与多所 985 有联合培养项目，是考研的隐形跳板。

复习资源：南湖校区图书馆四楼是考研党聚集地，氛围浓厚到让人不敢玩手机。学校有专门的考研自习室，需要提前申请座位。考研经验分享会每学期 2-3 场。

关键时间线：大三下学期开始准备，暑假是黄金复习期。矿大期末考试通常在 12 月底，与考研时间接近，需要提前规划。

如果高考失利没考上理想学校，矿大是一个很好的"中转站"。学风会逼着你往前走。',
 63, 28, '考研,双一流,逆袭'),

('exp-7', 'career', (SELECT id FROM school WHERE code='nuaa'), '南京航空航天大学', '南京',
 '南航毕业生都去哪了——就业数据与个人观察',
 '南航的就业率在江苏高校中保持领先。航天、军工、互联网是三大主流去向，但也有不少同学选择了出人意料的路径……',
 '南京航空航天大学毕业生就业质量报告年年亮眼。航空航天类专业进国企/科研院所比例高（约 40%），计算机/软件类专业多去互联网大厂。

热门雇主：中国商飞、航天科技集团、航空工业集团、华为、中兴。南航在军工和制造业的口碑很硬，简历关基本不会被筛。

实习资源：南航与南京江宁区的众多企业有合作。大三暑期实习是进入大厂的最佳窗口。学校就业指导中心每周发布实习信息，建议从大一就开始关注。

薪资水平：工科应届生起薪普遍在 12-18 万/年（含年终），计算机方向更高。国企起薪低但福利好，五年后总收入差距不大。

非技术方向的同学也不用担心——南航的品牌效应在江苏就业市场足够强，银行、快消、公务员都有不少校友。',
 48, 22, '就业,航天,工科'),

('exp-8', 'dorm', (SELECT id FROM school WHERE code='ujs'), '江苏大学', '镇江',
 '江苏大学宿舍改造记——把四人寝变成温馨小窝',
 '江大的宿舍基础条件一般，但花 200 块钱和一下午的时间，就能把普通四人寝改造得舒适又好看……',
 '江苏大学本部宿舍以四人间为主，部分是六人间。基础配置：上下铺 + 独立书桌 + 空调 + 公共卫浴。

改造清单（总花费 ~200 元）：
1. 床帘 + 支架（60 元）——创造私密空间，遮光效果好
2. LED 灯串（20 元）——贴在书桌上方，氛围感拉满
3. 桌上置物架（30 元）——收纳神器，桌面瞬间整洁
4. 泡沫地垫（50 元）——冬天光脚不冷，室友可以一起坐地上聊天
5. 门后挂钩（15 元）——挂书包、外套、雨伞

宿舍关系建议：第一周就制定好值日表和熄灯时间，避免日后摩擦。江大的宿管阿姨普遍好说话，偶尔晚归说一声就行。

本部宿舍虽然不新，但胜在离教学楼和食堂都近，五分钟步行覆盖全部。',
 29, 12, '宿舍改造,镇江,生活'),

('exp-9', 'freshman', (SELECT id FROM school WHERE code='xjtlu'), '西交利物浦大学', '苏州',
 '中外合作大学的真实体验——西交利物浦到底值不值',
 '一年学费 8.8 万，全英文授课，90% 出国读研——西浦的 tagline 很诱人，但真实体验和营销话术之间有差距……',
 '西交利物浦大学位于苏州工业园区，校园建筑现代感十足。全英文授课是真的，大一就要适应全英文学术环境，对英语基础要求很高。

教学模式与国内高校完全不同：没有辅导员、没有固定班级、没有早晚自习。选课制和导师制意味着很强的自主性——自律差的同学可能会跟不上。

出国深造率确实很高（约 85% 去 QS 前 100），但背后是高昂的经济投入。四年学费约 35 万，加上留学硕士又是 30-50 万。家庭经济压力要考虑清楚。

社交方面：学生来自全国各地，国际化程度高（约 10% 国际生）。校园活动丰富但学术竞争也激烈，GPA 压分是公开的秘密。

适合人群：英语好、自律、家庭经济宽裕、有明确出国规划的同学。不适合：随大流选校、英语吃力、喜欢传统校园氛围的同学。',
 55, 36, '中外合作,苏州,择校'),

('exp-10', 'study', (SELECT id FROM school WHERE code='cpu'), '中国药科大学', '南京',
 '药学这条路——从药大本科到医药行业的真实路径',
 '药学不是「化学+生物」那么简单。药大的课程设置、实验强度和就业方向，来之前你必须知道……',
 '中国药科大学是中国药学教育的最高学府。江宁校区环境优美，实验室设备全国顶尖。

课业强度：大一大二基础课（无机化学、有机化学、生物化学、生理学），实验课占比很高，每周至少两个下午泡在实验室。大三大四进入专业课（药剂学、药理学、药物化学、药物分析），难度跃升。

就业方向：
- 药企研发（恒瑞、正大天晴等）：硕士起步，博士更好
- 医院药房：稳定但收入天花板低
- 药品注册/临床监查：女生选择较多的方向
- 医药代表：收入高但压力大

药大的保研率约 20%，考研气氛浓。如果你想做科研，建议从大二就进实验室跟导师做项目。药学专业考研竞争激烈，但药大背景加成明显。',
 37, 16, '药学,专业选择,医药');

-- ── 8条问答 ──
INSERT INTO `qa_entry` (`code`, `question`, `answer`, `school_id`, `school_name`, `category`, `like_count`) VALUES
('qa-1', '南大和东大的宿舍条件哪个更好？',
 '整体来说南大仙林校区略优于东大九龙湖校区。南大仙林以四人间为主，独卫独浴配置齐全；东大九龙湖也是四人间但部分楼栋是公共卫浴。两校新校区宿舍都不错，差别在细节：南大热水 24h 供应，东大有时段限制。如果在意住宿体验，南大仙林更推荐。',
 (SELECT id FROM school WHERE code='nju'), '南京大学', 'dorm', 42),

('qa-2', '江苏高校的考研氛围怎么样？',
 '江苏高校整体考研氛围浓厚，尤其是双一流和省属重点院校。中国矿业大学（徐州）考研率超过 60%，南京师范大学、江苏大学、扬州大学等也是考研大户。南京的 985/211 高校保研率较高（15%-30%），考研压力相对小一些。如果目标是考研上岸，江苏的双非一本是很好的跳板。',
 NULL, NULL, 'exam', 35),

('qa-3', '转专业难不难？哪些学校最灵活？',
 '转专业难度因学校差异很大。省内转专业政策最宽松的是南京大学（大类招生后自由选择专业方向）和苏州大学（大一末可申请转专业，成功率约 60%）。东南大学、南京航空航天大学转热门专业竞争激烈，通常要求大一绩点排名前 20%。省属本科院校转专业相对容易，但热门专业仍有门槛。建议入学前查阅目标学校的最新转专业实施办法。',
 NULL, NULL, 'study', 28),

('qa-4', '在南京上学一个月生活费大概多少？',
 '南京大学生月均生活费在 1500-2500 元之间，取决于消费习惯。食堂吃饭每月约 800-1200 元（一天三顿在食堂），加上水果零食 200 元、日用品及话费 200 元、社交娱乐 300-500 元。仙林和江宁的消费略低于鼓楼市中心。如果自己做饭（校外租房），伙食费可以压到 600 元左右。总体来说，2000 元/月在南京可以过得比较舒服。',
 (SELECT id FROM school WHERE code='nju'), '南京大学', 'city-life', 51),

('qa-5', '西交利物浦和昆山杜克怎么选？',
 '两校定位不同。西浦（西交利物浦）办学时间更长，在校生规模大，专业选择多，毕业生申请英国 G5 研究生有优势。昆山杜克小而精，师生比极低（1:7），课程更偏文理教育（Liberal Arts），美国杜克大学资源加持明显。费用上昆杜更贵（学费约 17 万/年 vs 西浦 8.8 万/年）。追求性价比和英国方向选西浦，追求精英教育和美国方向选昆杜。',
 NULL, NULL, 'study', 33),

('qa-6', '江苏的 985/211 分别有哪些？',
 '江苏共有 2 所 985 高校：南京大学、东南大学（均在南京）。11 所 211 高校：南京航空航天大学、南京理工大学、河海大学、南京农业大学、中国药科大学、南京师范大学、苏州大学、江南大学（无锡）、中国矿业大学（徐州），加上两所 985。此外还有 5 所双一流建设高校：南京信息工程大学、南京邮电大学、南京林业大学、南京医科大学、南京中医药大学。',
 NULL, NULL, 'freshman', 67),

('qa-7', '江苏高校毕业后留在当地工作的多吗？',
 '非常高。根据各校就业质量报告，江苏省内高校毕业生留苏就业比例普遍在 60%-80%。南京高校毕业生主要留在南京和苏州，苏州大学约 70% 毕业生留在苏州。江苏经济体量大、产业门类齐全（制造业、IT、医药、金融等），提供了充足的就业岗位。长三角一体化也让跨城就业（上海/杭州）非常方便。',
 NULL, NULL, 'career', 29),

('qa-8', '徐州和连云港的高校值得去吗？',
 '看你的目标。如果追求学术或考研跳板，中国矿业大学（徐州 211）性价比很高——分数线相对低但学风扎实，考研成功率极高。江苏海洋大学（连云港）的优势专业是海洋科学和水产养殖，在细分领域就业不错。两座城市的生活成本比南京低 30%-40%，但实习机会和城市活力确实不如苏南。如果你的分数刚好匹配，且不介意地理位置，这两所学校都值得认真考虑。',
 NULL, NULL, 'city-life', 25);

-- ── 初始管理员账户 (密码: admin123, BCrypt加密) ──
INSERT INTO `app_user` (`username`, `password`, `nickname`, `role`, `status`) VALUES
('admin', '$2a$10$PdDp26p/1KF.d8B.AKkTlO.H5YBbdw0gzyc1ECr09Q1XNT5KxSYA.', '系统管理员', 'ADMIN', 1);
