# 江苏高校生活指北可视化平台 - 后端 API 文档

## 基本信息

| 项目 | 说明 |
|------|------|
| 框架 | Spring Boot 3.2.5 + Java 17 |
| 数据库 | H2 (开发) / MySQL (生产) |
| 端口 | 9090 |
| 认证 | JWT Token (Bearer) |
| 基础路径 | `http://localhost:9090` |

---

## 已实现功能总览

- 用户认证（注册/登录/JWT）
- 城市列表（13 个江苏地级市，含初始数据）
- 学校 CRUD + 搜索筛选
- 学校生活信息（宿舍/食堂/交通/学习氛围/周边）
- 评论留言（支持匿名、点赞）
- 投稿审核（用户投稿 → 管理员审批）
- 收藏功能
- 管理后台（Dashboard、用户/学校/评论/投稿管理）
- Swagger API 文档
- H2 数据库控制台

---

## 数据库设计（7 张表）

| 表名 | 说明 | 主要字段 |
|------|------|---------|
| `user` | 用户表 | username, password(BCrypt), nickname, role(USER/ADMIN) |
| `city` | 城市表 | name, pinyin, description, school_count, map_x, map_y |
| `school` | 学校表 | name, city_id, type, level, hot_score, favorite_count |
| `school_life_info` | 生活信息表 | dorm_score, canteen_score, study_score, transport_score |
| `comment` | 评论表 | school_id, user_id, content, category, is_anonymous |
| `submission` | 投稿表 | user_id, school_id, type, content, status(PENDING/APPROVED/REJECTED) |
| `favorite` | 收藏表 | user_id, school_id |

---

## API 接口列表

### 1. 认证接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/auth/login` | 用户登录 | 否 |
| POST | `/api/auth/register` | 用户注册 | 否 |
| GET | `/api/auth/me` | 获取当前用户信息 | 是 |
| POST | `/api/auth/logout` | 退出登录 | 是 |

**登录请求示例：**
```json
POST /api/auth/login
{
    "username": "admin",
    "password": "admin123"
}
```

**登录响应：**
```json
{
    "code": 200,
    "data": {
        "userId": 1,
        "username": "admin",
        "nickname": "系统管理员",
        "role": "ADMIN",
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
    }
}
```

### 2. 城市接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/cities` | 获取所有城市 | 否 |
| GET | `/api/cities/{id}` | 获取城市详情 | 否 |
| GET | `/api/cities/{id}/schools` | 获取城市下的学校 | 否 |
| GET | `/api/cities/{id}/detail-stats` | 城市统计信息 | 否 |
| GET | `/api/cities/stats` | 平台总体统计 | 否 |

### 3. 学校接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/schools` | 搜索/筛选学校 | 否 |
| GET | `/api/schools/{id}` | 学校详情(含生活信息) | 否 |
| GET | `/api/schools/hot` | 热门学校排行 | 否 |

**搜索参数：**
```
GET /api/schools?keyword=南京&city=1&level=985&page=0&size=20
```

### 4. 评论接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/schools/{id}/comments` | 获取学校评论 | 否 |
| POST | `/api/schools/{id}/comments` | 发表评论 | 是 |
| POST | `/api/comments/{id}/like` | 点赞评论 | 是 |

### 5. 投稿接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/submissions` | 提交投稿 | 是 |
| GET | `/api/user/submissions` | 我的投稿列表 | 是 |

**投稿类型：** `NEW_INFO` / `CORRECTION` / `EXPERIENCE` / `DORM` / `CANTEEN` / `TRANSPORT` / `STUDY` / `SURROUNDING`

### 6. 收藏接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/user/favorites` | 我的收藏 | 是 |
| POST | `/api/user/favorites/{schoolId}` | 收藏学校 | 是 |
| DELETE | `/api/user/favorites/{schoolId}` | 取消收藏 | 是 |

### 7. 管理后台接口（需要 ADMIN 角色）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/dashboard` | 仪表盘统计 |
| GET | `/api/admin/users` | 用户列表 |
| GET | `/api/admin/schools` | 学校列表 |
| POST | `/api/admin/schools` | 新增学校 |
| PUT | `/api/admin/schools/{id}` | 编辑学校 |
| DELETE | `/api/admin/schools/{id}` | 删除学校 |
| GET | `/api/admin/comments` | 评论列表(含待审核) |
| POST | `/api/admin/comments/{id}/approve` | 通过评论 |
| POST | `/api/admin/comments/{id}/reject` | 驳回评论 |
| GET | `/api/admin/submissions` | 投稿列表 |
| POST | `/api/admin/submissions/{id}/approve` | 通过投稿 |
| POST | `/api/admin/submissions/{id}/reject` | 驳回投稿 |
| POST | `/api/admin/cities` | 新增城市 |
| PUT | `/api/admin/cities/{id}` | 编辑城市 |

---

## 项目结构

```
backend/
├── pom.xml
├── src/main/java/com/jiangsu/guide/
│   ├── GuideApplication.java          # 启动入口
│   ├── config/                         # 配置类
│   ├── controller/
│   │   ├── AuthController.java         # 认证
│   │   ├── CityController.java         # 城市
│   │   ├── SchoolController.java       # 学校
│   │   ├── CommentController.java      # 评论
│   │   ├── SubmissionController.java   # 投稿
│   │   ├── FavoriteController.java     # 收藏
│   │   └── AdminController.java        # 管理后台
│   ├── service/
│   │   └── impl/
│   │       ├── AuthServiceImpl.java
│   │       ├── CityServiceImpl.java
│   │       ├── SchoolServiceImpl.java
│   │       ├── CommentServiceImpl.java
│   │       ├── SubmissionServiceImpl.java
│   │       └── FavoriteServiceImpl.java
│   ├── entity/                         # 7个实体类
│   ├── repository/                     # 7个数据访问接口
│   ├── dto/                            # 9个数据传输对象
│   ├── security/
│   │   ├── SecurityConfig.java         # Spring Security配置
│   │   ├── JwtTokenProvider.java       # JWT令牌工具
│   │   └── JwtAuthenticationFilter.java# JWT认证过滤器
│   ├── common/                         # 工具类
│   └── exception/                      # 异常处理
└── src/main/resources/
    ├── application.yml                 # 应用配置
    └── db/schema.sql                   # 数据库初始化脚本
```

---

## 开发工具地址

| 工具 | 地址 |
|------|------|
| Swagger API 文档 | http://localhost:9090/api/docs |
| H2 数据库控制台 | http://localhost:9090/h2-console |
| JDK 路径 | `C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot` |
| Maven 路径 | `C:\Users\Administrator\apache-maven-3.9.9` |

### H2 控制台连接信息

| 字段 | 值 |
|------|-----|
| JDBC URL | `jdbc:h2:mem:jiangsu_guide` |
| 用户名 | `sa` |
| 密码 | 留空 |

---

## 默认账户

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员(ADMIN) |

---

## 部署方式

### 开发环境
```bash
cd backend
mvn spring-boot:run
```

### 生产环境（需先安装 MySQL）
修改 `application.yml` 中的数据源配置为 MySQL 连接信息。

### IntelliJ IDEA
打开 `backend/` 目录，IDEA 会自动识别 Maven 项目，运行 `GuideApplication.main()`。

---

## 响应格式

所有接口统一返回：
```json
{
    "code": 200,        // 200成功, 400参数错误, 401未认证, 403权限不足, 404不存在
    "message": "success",
    "data": {}          // 具体数据
}
```

---

## 认证方式

在请求头中添加：
```
Authorization: Bearer <token>
```

Token 有效期 24 小时，RefreshToken 有效期 7 天。

---

## 下一步计划（V2.0）

- [ ] Redis 缓存热点数据
- [ ] 搜索引擎优化
- [ ] 30 所高校完整数据
- [ ] 移动端适配
- [ ] 高校对比功能
- [ ] 用户等级体系
- [ ] 消息队列处理投稿
